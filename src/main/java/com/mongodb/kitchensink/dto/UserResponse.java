package com.mongodb.kitchensink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@Schema(description = "Response payload for user details")
public class UserResponse {
    @Schema(description = "ID of the user", example = "12345")
    private String id;

    @Schema(description = "Name of the user", example = "John Doe")
    private String name;

    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Phone number of the user", example = "9876543210")
    private String phone;
}