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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecoembes.entity.Dumpster;
import com.ecoembes.entity.DumpsterReading;
import com.ecoembes.entity.FillLevel;
import com.ecoembes.service.AuthService;
import com.ecoembes.service.DumpsterService;

import com.ecoembes.dto.DumpsterDTO;
import com.ecoembes.dto.DumpsterStatusDTO;
import com.ecoembes.dto.DumpsterUpdateDTO;
import com.ecoembes.dto.DumpsterUsageDTO;
import com.ecoembes.dto.NewDumpsterDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/dumpsters")
@Tag(name = "Dumpsters", description = "Dumpster sensor updates and dumpster queries")
public class DumpsterController {

    private final DumpsterService dumpsterService;
    private final AuthService authService;

    public DumpsterController(DumpsterService dumpsterService, AuthService authService) {
        this.dumpsterService = dumpsterService;
        this.authService = authService;
    }

    @Operation(summary = "Update dumpster info (sensor)",
               description = "Daily update sent by the dumpster sensor at 3 AM. No token: it is a device, not an employee")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "No Content: reading stored"),
        @ApiResponse(responseCode = "400", description = "Bad Request: invalid data"),
        @ApiResponse(responseCode = "404", description = "Not Found: unknown dumpster")
    })
    @PutMapping("/{dumpsterId}/info")
    public ResponseEntity<Void> updateDumpsterInfo(@PathVariable String dumpsterId,
                                                   @RequestBody DumpsterUpdateDTO info) {
        try {
            dumpsterService.updateDumpsterInfo(dumpsterId, info.estimatedContainers(), info.fillLevel(), info.date());
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Create a new dumpster")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Bad Request: invalid data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized: invalid token"),
        @ApiResponse(responseCode = "409", description = "Conflict: id already exists")
    })
    @PostMapping
    public ResponseEntity<DumpsterDTO> createDumpster(@RequestParam String token,
                                                      @RequestBody NewDumpsterDTO dumpster) {
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Dumpster created = dumpsterService.createDumpster(new Dumpster(
                    dumpster.id(), dumpster.address(), dumpster.postalCode(), dumpster.capacity()));
            return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(created));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get all dumpsters", description = "Location, number of containers and current fill level")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Unauthorized: invalid token")
    })
    @GetMapping
    public ResponseEntity<List<DumpsterDTO>> getDumpsters(@RequestParam String token) {
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(dumpsterService.getDumpsters().stream().map(this::toDTO).toList());
    }

    @Operation(summary = "Query dumpster usage", description = "Readings of a dumpster between startDate and endDate (yyyy-MM-dd)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request: invalid dates"),
        @ApiResponse(responseCode = "401", description = "Unauthorized: invalid token"),
        @ApiResponse(responseCode = "404", description = "Not Found: unknown dumpster")
    })
    @GetMapping("/{dumpsterId}/usage")
    public ResponseEntity<List<DumpsterUsageDTO>> queryDumpsterUsage(
            @PathVariable String dumpsterId,
            @RequestParam String token,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            List<DumpsterUsageDTO> usage = dumpsterService.getUsage(dumpsterId, startDate, endDate).stream()
                    .map(r -> new DumpsterUsageDTO(r.getDate(), r.getEstimatedContainers(), r.getFillLevel()))
                    .toList();
            return ResponseEntity.ok(usage);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Check dumpster status", description = "Status of the dumpsters of a postal code on a given date (yyyy-MM-dd)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized: invalid token")
    })
    @GetMapping("/status")
    public ResponseEntity<List<DumpsterStatusDTO>> checkDumpsterStatus(
            @RequestParam String token,
            @RequestParam String postalCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (!authService.isValidToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            List<DumpsterStatusDTO> status = dumpsterService.getStatus(postalCode, date).stream()
                    .map(d -> {
                        DumpsterReading r = d.getReading(date);
                        return new DumpsterStatusDTO(d.getId(), d.getAddress(), r.getEstimatedContainers(), r.getFillLevel());
                    })
                    .toList();
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private DumpsterDTO toDTO(Dumpster d) {
        DumpsterReading last = d.getLastReading();
        return new DumpsterDTO(d.getId(), d.getAddress(), d.getPostalCode(), d.getCapacity(),
                last == null ? 0 : last.getEstimatedContainers(),
                last == null ? FillLevel.GREEN : last.getFillLevel());
    }
}
