package com.mongodb.kitchensink.controller;


import com.mongodb.kitchensink.dto.UserUpdateRequest;
import com.mongodb.kitchensink.exception.InvalidFieldException;
import com.mongodb.kitchensink.service.UserInfoService;
import com.mongodb.kitchensink.service.UserValidation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/rest/members")
@Tag(name = "User Management", description = "APIs for creating, updating, fetching, and deleting users")
@Slf4j
public class UserController {

    private final UserInfoService service;
    private final UserValidation validation;

    public UserController(UserInfoService service, UserValidation validation) {
        this.service = service;
        this.validation = validation;
    }

    // -------- PUBLIC ENDPOINTS --------

    @GetMapping("/check")
    public String healthCheck() {
        log.info("Health check endpoint called");
        return "API is working!";
    }

    // -------- PROTECTED ENDPOINTS --------

//    @GetMapping("/all")
//    @SecurityRequirement(name = "bearerAuth")
//    @Operation(summary = "Get paginated list of users", description = "Returns users in paginated format.")
//    public ResponseEntity<?> getAllUsers(@PageableDefault(page = 0, size = 10) Pageable pageable, Authentication authentication) {
//        log.info("Fetching users with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
//        validation.validateAdmin(authentication);
//        return ResponseEntity.ok(service.getAll(pageable));
//    }

    @GetMapping("/all")
    @SecurityRequirement(name = "bearerAuth")
    public Page<?> getAllUsers(
            @PageableDefault(page=0, size = 10) Pageable pageable,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) {

        ZoneId zone = ZoneId.of("Asia/Kolkata");
        Instant fromInstant = null, toInstant = null;

        if (from != null) {
            fromInstant = from.atStartOfDay(zone).toInstant();
        }
        if (to != null) {
            toInstant = to.plusDays(1).atStartOfDay(zone).toInstant().minusNanos(1);
        }
        if (fromInstant != null && toInstant != null && fromInstant.isAfter(toInstant)) {
            throw new InvalidFieldException("from must be before to");
        }
        log.info("Fetching users with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        validation.validateAdmin(authentication);
        return service.getAllFiltered(pageable, fromInstant, toInstant);
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get list of users", description = "Returns users")
    public ResponseEntity<?> getAllUsers(Authentication authentication) {
        log.info("Inside getAllUsers :: Fetching all users");
        validation.validateAdmin(authentication);
        return ResponseEntity.ok(service.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Fetches a single user by their unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)})
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getUserDetails(@PathVariable String id, Authentication authentication) {
        log.info("Fetching user with ID: {}", id);
        validation.validateAdminOrUserById(id, authentication);
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update an existing user", description = "Updates details of a registered user by id")
    public ResponseEntity<?> updateUser(@PathVariable String id, @Valid @RequestBody UserUpdateRequest req, Authentication authentication) {
        log.info("Updating user with Email: {}", req.getEmail());
        validation.validateAdminOrUserById(id, authentication);
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete user by ID", description = "Removes a user from the system (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID supplied"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")})
    public ResponseEntity<Void> deleteUser(@PathVariable String id, Authentication authentication) {
        log.info("Deleting user with ID: {}", id);
        validation.validateDeleteAdminOrUserById(id, authentication);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        log.info("Fetching current user: {}", authentication.getName());
        return ResponseEntity.ok(service.getByEmail(authentication.getName()));
    }

    @GetMapping("/search")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Search users",
            description = "Search by email and/or name (case-insensitive, contains). At least one parameter is required."
    )
    public ResponseEntity<?> searchUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false, name = "name") String name,
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            Authentication authentication) {

        boolean noEmail = (email == null || email.isBlank());
        boolean noName  = (name  == null || name.isBlank());
        if (noEmail && noName) {
            throw new InvalidFieldException("At least one of 'email' or 'name' must be provided.");
        }
        validation.validateAdmin(authentication);
        return ResponseEntity.ok(service.search(email, name, pageable));
    }

    @GetMapping("/by-email")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get user by email", description = "Exact email match.")
    public ResponseEntity<?> getByEmailExact(@RequestParam String email, Authentication authentication) {
        validation.validateAdmin(authentication);
        return ResponseEntity.ok(service.getByEmail(email));
    }
}