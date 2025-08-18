package com.mongodb.kitchensink.repository;

import com.mongodb.kitchensink.model.UserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.Optional;

public interface UserInfoRepository extends MongoRepository<UserInfo, String> {
    boolean existsByEmail(String email);

    Optional<UserInfo> findByEmail(String email);

    Optional<UserInfo> findByEmailAndPhone(String email, String phoneNumber);

    Page<UserInfo> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    Page<UserInfo> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    Page<UserInfo> findByEmailContainingIgnoreCaseAndUsernameContainingIgnoreCase(String email, String username, Pageable pageable);

    Page<UserInfo> findAllByCreatedAtBetween(Instant from, Instant to, Pageable pageable);
    Page<UserInfo> findAllByCreatedAtAfter(Instant from, Pageable pageable);
    Page<UserInfo> findAllByCreatedAtBefore(Instant to, Pageable pageable);
}