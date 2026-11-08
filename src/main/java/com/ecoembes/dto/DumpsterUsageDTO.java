package com.ecoembes.dto;

import java.time.LocalDate;

import com.ecoembes.entity.FillLevel;

public record DumpsterUsageDTO(LocalDate date, int estimatedContainers, FillLevel fillLevel) {}
