package com.ecoembes.facade;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecoembes.service.AuthService;

import com.ecoembes.dto.CredentialsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Employee login and logout")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Login", description = "Returns a token (server timestamp) to use in the rest of the operations")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK: token returned"),
        @ApiResponse(responseCode = "401", description = "Unauthorized: wrong email or password")
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody CredentialsDTO credentials) {
        return authService.login(credentials.email(), credentials.password())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Operation(summary = "Logout", description = "Deletes the token; it can no longer be used")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "No Content: logout done"),
        @ApiResponse(responseCode = "401", description = "Unauthorized: invalid token")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam String token) {
        return authService.logout(token)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
