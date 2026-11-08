package com.ecoembes.dto;

import com.ecoembes.entity.FillLevel;

public record DumpsterStatusDTO(String id, String address, int estimatedContainers, FillLevel fillLevel) {}
