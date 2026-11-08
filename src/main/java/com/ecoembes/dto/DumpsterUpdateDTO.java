package com.ecoembes.dto;

import java.time.LocalDate;

import com.ecoembes.entity.FillLevel;

/** Data sent daily by the dumpster sensor. If date is null, today is used. */
public record DumpsterUpdateDTO(int estimatedContainers, FillLevel fillLevel, LocalDate date) {}
