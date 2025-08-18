package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.dto.UserRequest;
import com.mongodb.kitchensink.dto.UserResponse;
import com.mongodb.kitchensink.dto.UserUpdateRequest;
import com.mongodb.kitchensink.exception.UserDeletionException;
import com.mongodb.kitchensink.exception.UserNotFoundException;
import com.mongodb.kitchensink.model.UserInfo;
import com.mongodb.kitchensink.repository.UserInfoRepository;
import com.mongodb.kitchensink.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserInfoServiceImpl implements UserInfoService {

    private final PasswordEncoder passwordEncoder;
    private final UserInfoRepository userRepo;

    public UserInfoServiceImpl(PasswordEncoder passwordEncoder, UserInfoRepository userRepo) {
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
    }

    @Override
    @Transactional
    public void createUser(UserRequest userRequest) {
        final String normEmail = normEmail(userRequest.getEmail());
        final String normPhone = normPhone(userRequest.getPhone());
        final String normName  = normText(userRequest.getUsername());

        UserInfo user = new UserInfo();
        user.setEmail(normEmail);
        user.setPhone(normPhone);
        user.setUsername(normName);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRoles(Constants.ROLES_USER);

        userRepo.save(user);
        log.info("Created user with id = {} & email = {}", user.getId(), user.getEmail());
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepo.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getPhone()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Page<UserResponse> getAll(Pageable pageable) {
        return userRepo.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public UserResponse getById(String id) {
        UserInfo user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(Constants.USER_NOT_FOUND));
        return mapToResponse(user);
    }

    @Override
    public UserResponse getByEmailAndPhone(String email, String phone) {
        final String normEmail = normEmail(email);
        final String normPhone = normPhone(phone);

        UserInfo user = userRepo.findByEmailAndPhone(normEmail, normPhone)
                .orElseThrow(() -> new UserNotFoundException(Constants.USER_NOT_FOUND));
        return mapToResponse(user);
    }

    @Override
    public UserResponse getByEmail(String email) {
        final String normEmail = normEmail(email);

        UserInfo user = userRepo.findByEmail(normEmail)
                .orElseThrow(() -> new UserNotFoundException(Constants.USER_NOT_FOUND));
        return mapToResponse(user);
    }
//Update by ID, not by email
    @Override
    @Transactional
    public UserResponse update(String id, UserUpdateRequest req) {
        log.info("Attempting to update user {}", id);

        UserInfo user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(Constants.USER_NOT_FOUND));

        if (hasText(req.getName()))  user.setUsername(normText(req.getName()));
        if (hasText(req.getPhone())) user.setPhone(normPhone(req.getPhone()));
        if (hasText(req.getPassword())) user.setPassword(passwordEncoder.encode(req.getPassword()));

        userRepo.save(user);
        log.info("Updated user with Email={}", user.getEmail());
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public void delete(String id) {
        log.info("Delete request for user with id = {}", id);
        UserInfo user = userRepo.findById(id)
                .orElseThrow(() -> new UserDeletionException("User not found"));
        userRepo.deleteById(user.getId());
    }

    private UserResponse mapToResponse(UserInfo user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .name(user.getUsername())
                .build();
    }

    @Override
    public Page<UserResponse> search(String emailLike, String nameLike, Pageable pageable) {
        boolean hasEmail = emailLike != null && !emailLike.isBlank();
        boolean hasName  = nameLike  != null && !nameLike.isBlank();

        final String e = hasEmail ? normEmail(emailLike) : null;
        final String n = hasName  ? normText(nameLike)  : null;

        Page<UserInfo> page;
        if (hasEmail && hasName) {
            page = userRepo.findByEmailContainingIgnoreCaseAndUsernameContainingIgnoreCase(e, n, pageable);
        } else if (hasEmail) {
            page = userRepo.findByEmailContainingIgnoreCase(e, pageable);
        } else if (hasName) {
            page = userRepo.findByUsernameContainingIgnoreCase(n, pageable);
        } else {
            page = userRepo.findAll(pageable);
        }

        return page.map(this::mapToResponse);
    }

    public Page<UserResponse> getAllFiltered(
            Pageable pageable, Instant from, Instant to) {

        Page<UserInfo> page;
        if (from != null && to != null) {
            page = userRepo.findAllByCreatedAtBetween(from, to, pageable);
        } else if (from != null) {
            page = userRepo.findAllByCreatedAtAfter(from, pageable);
        } else if (to != null) {
            page = userRepo.findAllByCreatedAtBefore(to, pageable);
        } else {
            page = userRepo.findAll(pageable);
        }
        return page.map(this::mapToResponse);
    }

    private static String normEmail(String s) {
        return (s == null) ? null : s.trim().toLowerCase(Locale.ROOT);
    }

    private static String normPhone(String s) {
        return (s == null) ? null : s.replaceAll("\\D+", "");
    }

    private static String normText(String s) {
        return (s == null) ? null : s.trim();
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}

