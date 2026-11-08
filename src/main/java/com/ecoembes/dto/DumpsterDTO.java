package com.ecoembes.dto;

import com.ecoembes.entity.FillLevel;

public record DumpsterDTO(String id, String address, String postalCode, int capacity,
                          int estimatedContainers, FillLevel fillLevel) {}
