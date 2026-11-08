package com.ecoembes.dto;

import java.time.LocalDate;

public record PlantCapacityDTO(String plantId, LocalDate date, float capacityTons) {}
