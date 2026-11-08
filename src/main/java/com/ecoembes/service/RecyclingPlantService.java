package com.ecoembes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecoembes.entity.Assignment;
import com.ecoembes.entity.Dumpster;
import com.ecoembes.entity.Employee;
import com.ecoembes.entity.RecyclingPlant;

@Service
public class RecyclingPlantService {

    private static final Logger log = LoggerFactory.getLogger(RecyclingPlantService.class);

    /** Assumption: average weight of one packaging container = 1 kg. */
    public static final float TONS_PER_CONTAINER = 0.001f;

    // Simulated repository (no persistence in TW1). In TW2 the capacity will be requested
    // to the PlasSB server (REST) and to the ContSocket server (sockets).
    private final Map<String, RecyclingPlant> plants = new LinkedHashMap<>();
    private final List<Assignment> assignments = new ArrayList<>();
    private final AtomicLong nextAssignmentId = new AtomicLong(1);

    private final DumpsterService dumpsterService;

    public RecyclingPlantService(DumpsterService dumpsterService) {
        this.dumpsterService = dumpsterService;

        RecyclingPlant plasSB = new RecyclingPlant("PlasSB", "PlasSB Ltd.");
        RecyclingPlant contSocket = new RecyclingPlant("ContSocket", "ContSocket Ltd.");
        // Capacity is always available 10 days in advance.
        LocalDate today = LocalDate.now();
        for (int i = 0; i <= 10; i++) {
            plasSB.setCapacity(today.plusDays(i), 5.0f);
            contSocket.setCapacity(today.plusDays(i), 2.5f);
        }
        plants.put(plasSB.getId(), plasSB);
        plants.put(contSocket.getId(), contSocket);
    }

    public List<RecyclingPlant> getRecyclingPlants() {
        return new ArrayList<>(plants.values());
    }

    private RecyclingPlant getPlant(String plantId) {
        RecyclingPlant plant = plants.get(plantId);
        if (plant == null) {
            throw new NoSuchElementException("Recycling plant " + plantId + " not found");
        }
        return plant;
    }

    /** Available capacity in tons: plant capacity minus what is already assigned that day. */
    public synchronized float getCapacity(String plantId, LocalDate date) {
        RecyclingPlant plant = getPlant(plantId);
        int assignedContainers = assignments.stream()
                .filter(a -> a.getPlant() == plant && a.getDate().equals(date))
                .mapToInt(Assignment::getTotalContainers)
                .sum();
        return Math.max(0f, plant.getCapacity(date) - assignedContainers * TONS_PER_CONTAINER);
    }

    public synchronized Assignment assignDumpsters(String plantId, List<String> dumpsterIds,
                                                   LocalDate date, Employee employee) {
        RecyclingPlant plant = getPlant(plantId);
        if (dumpsterIds == null || dumpsterIds.isEmpty()) {
            throw new IllegalArgumentException("At least one dumpster is required");
        }
        LocalDate day = date != null ? date : LocalDate.now();

        List<Dumpster> selected = dumpsterIds.stream().distinct().map(dumpsterService::getDumpster).toList();
        int totalContainers = selected.stream()
                .map(Dumpster::getLastReading)
                .mapToInt(r -> r == null ? 0 : r.getEstimatedContainers())
                .sum();

        float neededTons = totalContainers * TONS_PER_CONTAINER;
        float availableTons = getCapacity(plantId, day);
        if (neededTons > availableTons) {
            throw new IllegalStateException(String.format(
                    "Insufficient capacity: %.3f t needed, %.3f t available", neededTons, availableTons));
        }

        Assignment assignment = new Assignment(nextAssignmentId.getAndIncrement(), day,
                totalContainers, employee, plant, selected);
        assignments.add(assignment);
        notifyPlant(assignment);
        return assignment;
    }

    /** Simulated in TW1: in TW2 it will call the PlasSB / ContSocket servers. */
    private void notifyPlant(Assignment assignment) {
        log.info("[NOTIFICATION -> {}] {}: {} dumpsters, {} containers (assigned by {})",
                assignment.getPlant().getName(), assignment.getDate(), assignment.getNumDumpsters(),
                assignment.getTotalContainers(), assignment.getEmployee().getEmail());
    }
}
