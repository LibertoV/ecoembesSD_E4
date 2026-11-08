package com.ecoembes.facade;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecoembes.entity.Assignment;
import com.ecoembes.entity.Employee;
import com.ecoembes.service.AuthService;
import com.ecoembes.service.RecyclingPlantService;

import com.ecoembes.dto.AssignmentDTO;
import com.ecoembes.dto.AssignmentRequestDTO;
import com.ecoembes.dto.PlantCapacityDTO;
import com.ecoembes.dto.RecyclingPlantDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/plants")
@Tag(name = "Recycling plants", description = "Plant capacity and dumpster assignment")
public class RecyclingPlantController {

    private final RecyclingPlantService plantService;
    private final AuthService authService;

    public RecyclingPlantController(RecyclingPlantService plantService, AuthService authService) {
        this.plantService = plantService;
        this.authService = authService;
    }

    @Operation(summary = "Get recycling plants")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Unauthorized: invalid token")
    })
    @GetMapping
    public ResponseEntity<List<RecyclingPlantDTO>> getRecyclingPlants(@RequestParam String token) {
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(plantService.getRecyclingPlants().stream()
                .map(p -> new RecyclingPlantDTO(p.getId(), p.getName()))
                .toList());
    }

    @Operation(summary = "Check recycling plant capacity", description = "Available capacity in tons for a date (yyyy-MM-dd)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Unauthorized: invalid token"),
        @ApiResponse(responseCode = "404", description = "Not Found: unknown plant")
    })
    @GetMapping("/{plantId}/capacity")
    public ResponseEntity<PlantCapacityDTO> checkPlantCapacity(
            @PathVariable String plantId,
            @RequestParam String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return ResponseEntity.ok(new PlantCapacityDTO(plantId, date, plantService.getCapacity(plantId, date)));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Assign dumpsters to a recycling plant",
               description = "The plant is notified (simulated in TW1) and the employee is recorded for auditing")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created: assignment recorded"),
        @ApiResponse(responseCode = "400", description = "Bad Request: no dumpsters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized: invalid token"),
        @ApiResponse(responseCode = "404", description = "Not Found: unknown plant or dumpster"),
        @ApiResponse(responseCode = "409", description = "Conflict: insufficient capacity")
    })
    @PostMapping("/{plantId}/assignments")
    public ResponseEntity<AssignmentDTO> assignDumpsters(@PathVariable String plantId,
                                                         @RequestParam String token,
                                                         @RequestBody AssignmentRequestDTO request) {
        Employee employee = authService.getEmployeeByToken(token);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Assignment a = plantService.assignDumpsters(plantId, request.dumpsterIds(), request.date(), employee);
            return ResponseEntity.status(HttpStatus.CREATED).body(new AssignmentDTO(a.getId(), a.getPlant().getId(),
                    a.getDate(), a.getEmployee().getEmail(), a.getNumDumpsters(), a.getTotalContainers()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
