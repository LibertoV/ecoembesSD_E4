package com.ecoembes.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Dumpster {

    private String id;
    private String address;
    private String postalCode;
    private int capacity; // maximum number of containers
    private List<DumpsterReading> readings = new ArrayList<>();

    public Dumpster(String id, String address, String postalCode, int capacity) {
        this.id = id;
        this.address = address;
        this.postalCode = postalCode;
        this.capacity = capacity;
    }

    public String getId() { return id; }
    public String getAddress() { return address; }
    public String getPostalCode() { return postalCode; }
    public int getCapacity() { return capacity; }
    public List<DumpsterReading> getReadings() { return readings; }

    /** Adds a reading; if one already exists for that date it is replaced. */
    public void addReading(DumpsterReading reading) {
        readings.removeIf(r -> r.getDate().equals(reading.getDate()));
        readings.add(reading);
        readings.sort(Comparator.comparing(DumpsterReading::getDate));
    }

    /** Most recent reading, or null if the sensor has not sent any. */
    public DumpsterReading getLastReading() {
        return readings.isEmpty() ? null : readings.get(readings.size() - 1);
    }

    /** Reading of a given date, or null if there is none. */
    public DumpsterReading getReading(LocalDate date) {
        return readings.stream().filter(r -> r.getDate().equals(date)).findFirst().orElse(null);
    }
}
