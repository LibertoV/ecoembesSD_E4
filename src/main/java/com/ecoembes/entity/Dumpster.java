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

    public void addReading(DumpsterReading reading) {
        for (int i = readings.size() - 1; i >= 0; i--) {
            if (readings.get(i).getDate().equals(reading.getDate())) {
                readings.remove(i);
            }
        }
        readings.add(reading);
        readings.sort(Comparator.comparing(DumpsterReading::getDate));
    }

    public DumpsterReading getLastReading() {
        if (readings.isEmpty()) {
            return null;
        }
        return readings.get(readings.size() - 1);
    }

    public DumpsterReading getReading(LocalDate date) {
        for (DumpsterReading r : readings) {
            if (r.getDate().equals(date)) {
                return r;
            }
        }
        return null;
    }
}
