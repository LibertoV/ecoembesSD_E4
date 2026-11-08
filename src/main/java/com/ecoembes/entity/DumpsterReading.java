package com.ecoembes.entity;

import java.time.LocalDate;

/** Daily update sent by the dumpster sensor (simulated at 3 AM). */
public class DumpsterReading {

    private LocalDate date;
    private int estimatedContainers;
    private FillLevel fillLevel;

    public DumpsterReading(LocalDate date, int estimatedContainers, FillLevel fillLevel) {
        this.date = date;
        this.estimatedContainers = estimatedContainers;
        this.fillLevel = fillLevel;
    }

    public LocalDate getDate() { return date; }
    public int getEstimatedContainers() { return estimatedContainers; }
    public FillLevel getFillLevel() { return fillLevel; }
}
