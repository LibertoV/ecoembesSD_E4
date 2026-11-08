package com.ecoembes.entity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class RecyclingPlant {

    private String id;
    private String name;
    private Map<LocalDate, Float> capacities = new HashMap<>(); // tons per day

    public RecyclingPlant(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }

    public void setCapacity(LocalDate date, float tons) {
        capacities.put(date, tons);
    }

    /** Capacity in tons for a date; 0 if the plant has not provided it. */
    public float getCapacity(LocalDate date) {
        return capacities.getOrDefault(date, 0f);
    }
}
