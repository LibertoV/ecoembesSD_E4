package com.ecoembes.dto;

import java.time.LocalDate;
import java.util.List;

/** If date is null, today is used. */
public record AssignmentRequestDTO(List<String> dumpsterIds, LocalDate date) {}
