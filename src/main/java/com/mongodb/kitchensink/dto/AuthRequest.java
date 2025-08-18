package com.mongodb.kitchensink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Authentication request containing user credentials")
public class AuthRequest {

    @NotBlank(message = "Email is mandatory")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(com|co\\.in|org|net|in)$",
            message = "Invalid email format. Allowed domains: .com, .co.in, .org, .net, .in"
    )
    @Email
    @Schema(description = "Email used for login", example = "user@example.com")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Schema(description = "Password used for login", example = "StrongP@ssw0rd")
    private String password;

}