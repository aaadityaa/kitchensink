package com.mongodb.kitchensink.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class UserInfo {
    @Id
    private String id;
    private String username;
    private String password;
    @Indexed(unique = true)
    private String email;
    private String roles;
    private String phone;
    @CreatedDate
    @Indexed
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}
