package com.ecoembes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.ecoembes.entity.Dumpster;
import com.ecoembes.entity.DumpsterReading;
import com.ecoembes.entity.FillLevel;

@Service
public class DumpsterService {

    // Simulated repository (no persistence in TW1). Key = dumpster id.
    private final Map<String, Dumpster> dumpsters = new ConcurrentHashMap<>();

    public DumpsterService() {
        LocalDate today = LocalDate.now();
        Dumpster d1 = new Dumpster("D-001", "Avda. de las Universidades 24", "48007", 1000);
        Dumpster d2 = new Dumpster("D-002", "Calle Licenciado Poza 10", "48011", 800);
        Dumpster d3 = new Dumpster("D-003", "Gran Via 45", "48011", 1200);

        // Simulated sensor readings for the last 7 days (sent at 3 AM).
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            d1.addReading(new DumpsterReading(date, 100 + 120 * (6 - i), levelFor(100 + 120 * (6 - i), 1000)));
            d2.addReading(new DumpsterReading(date, 50 + 60 * (6 - i), levelFor(50 + 60 * (6 - i), 800)));
            d3.addReading(new DumpsterReading(date, 200 + 150 * (6 - i), levelFor(200 + 150 * (6 - i), 1200)));
        }
        dumpsters.put(d1.getId(), d1);
        dumpsters.put(d2.getId(), d2);
        dumpsters.put(d3.getId(), d3);
    }

    private static FillLevel levelFor(int containers, int capacity) {
        double ratio = (double) containers / capacity;
        if (ratio < 0.5) return FillLevel.GREEN;
        if (ratio < 0.9) return FillLevel.ORANGE;
        return FillLevel.RED;
    }

    /** Daily update from the sensor. */
    public void updateDumpsterInfo(String id, int containers, FillLevel level, LocalDate date) {
        Dumpster dumpster = getDumpster(id);
        if (containers < 0 || level == null) {
            throw new IllegalArgumentException("Invalid sensor data");
        }
        dumpster.addReading(new DumpsterReading(date != null ? date : LocalDate.now(), containers, level));
    }

    public Dumpster createDumpster(Dumpster dumpster) {
        if (dumpster.getId() == null || dumpster.getId().isBlank() || dumpster.getCapacity() <= 0) {
            throw new IllegalArgumentException("Id and a positive capacity are required");
        }
        if (dumpsters.putIfAbsent(dumpster.getId(), dumpster) != null) {
            throw new IllegalStateException("Dumpster " + dumpster.getId() + " already exists");
        }
        return dumpster;
    }

    public List<Dumpster> getDumpsters() {
        List<Dumpster> list = new ArrayList<>(dumpsters.values());
        list.sort(Comparator.comparing(Dumpster::getId));
        return list;
    }

    public Dumpster getDumpster(String id) {
        Dumpster dumpster = dumpsters.get(id);
        if (dumpster == null) {
            throw new NoSuchElementException("Dumpster " + id + " not found");
        }
        return dumpster;
    }

    /** Readings of a dumpster between two dates (both included). */
    public List<DumpsterReading> getUsage(String id, LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            throw new IllegalArgumentException("startDate must be before or equal to endDate");
        }
        return getDumpster(id).getReadings().stream()
                .filter(r -> !r.getDate().isBefore(start) && !r.getDate().isAfter(end))
                .toList();
    }

    /** Dumpsters of a postal code that have a reading for the given date. */
    public List<Dumpster> getStatus(String postalCode, LocalDate date) {
        if (postalCode == null || date == null) {
            throw new IllegalArgumentException("postalCode and date are required");
        }
        return dumpsters.values().stream()
                .filter(d -> d.getPostalCode().equals(postalCode))
                .filter(d -> d.getReading(date) != null)
                .sorted(Comparator.comparing(Dumpster::getId))
                .toList();
    }
}
