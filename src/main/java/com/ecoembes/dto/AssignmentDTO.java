package com.ecoembes.dto;

import java.time.LocalDate;

public record AssignmentDTO(long assignmentId, String plantId, LocalDate date, String employeeEmail,
                            int numDumpsters, int totalContainers) {}
