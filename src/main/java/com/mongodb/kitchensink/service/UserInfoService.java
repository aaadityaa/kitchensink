package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.dto.UserRequest;
import com.mongodb.kitchensink.dto.UserResponse;
import com.mongodb.kitchensink.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface UserInfoService {

    void createUser(UserRequest userRequest);

    Page<UserResponse> getAll(Pageable pageable);

    UserResponse getById(String id);

    UserResponse getByEmailAndPhone(String email, String phone);

    UserResponse getByEmail(String email);

    UserResponse update(String id, UserUpdateRequest req);

    void delete(String id);

    List<UserResponse> getAllUsers();

    Page<UserResponse> search(String emailLike, String nameLike, Instant from, Instant to, Pageable pageable);

    Page<UserResponse> getAllFiltered(Pageable pageable, Instant from, Instant to);
}