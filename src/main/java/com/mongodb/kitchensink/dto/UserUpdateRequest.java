package com.mongodb.kitchensink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request payload for creating or updating a user")
public class UserUpdateRequest {

        @Pattern(
                regexp = "^[A-Za-z][A-Za-z_ ]{2,49}$",
                message = "Username must start with a letter, can contain only letters, spaces, and underscores, and be 3-50 characters long"
        )
        @Schema(description = "Name for the user", example = "John_Doe")
        private String name;

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must be at least 8 characters long, with at least 1 uppercase, 1 lowercase, 1 digit, and 1 special character"
        )
        @Schema(description = "Secure password for the user", example = "P@ssw0rd123")
        @Size(min=8,max=72)
        private String password;

        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(com|co\\.in|org|net|in)$",
                message = "Invalid email format. Allowed domains: .com, .co.in, .org, .net, .in"
        )
        @Email
        @Schema(description = "Email address of the member", example = "john.doe@example.com")
        private String email;

        @Pattern(
                regexp = "^(?!(?:[6-9]0{9}))([6-9][0-9]{9})$",
                message = "Invalid Indian phone number. Must start with 6-9, be 10 digits, and not be all zeros after the first digit"
        )
        @Schema(description = "Phone number of the member", example = "9876543210")
        private String phone;
}
