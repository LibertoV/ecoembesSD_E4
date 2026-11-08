package com.ecoembes.entity;

import java.time.LocalDate;
import java.util.List;

/** Assignment of dumpsters to a recycling plant, recorded for auditing. */
public class Assignment {

    private long id;
    private LocalDate date;
    private int totalContainers;
    private Employee employee;      // performedBy
    private RecyclingPlant plant;
    private List<Dumpster> dumpsters;

    public Assignment(long id, LocalDate date, int totalContainers, Employee employee,
                      RecyclingPlant plant, List<Dumpster> dumpsters) {
        this.id = id;
        this.date = date;
        this.totalContainers = totalContainers;
        this.employee = employee;
        this.plant = plant;
        this.dumpsters = dumpsters;
    }

    public long getId() { return id; }
    public LocalDate getDate() { return date; }
    public int getTotalContainers() { return totalContainers; }
    public Employee getEmployee() { return employee; }
    public RecyclingPlant getPlant() { return plant; }
    public List<Dumpster> getDumpsters() { return dumpsters; }

    public int getNumDumpsters() {
        return dumpsters.size();
    }
}
