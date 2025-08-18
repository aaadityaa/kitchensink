package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.dto.UserRequest;
import com.mongodb.kitchensink.exception.UserNotFoundException;
import com.mongodb.kitchensink.model.UserInfo;
import com.mongodb.kitchensink.repository.UserInfoRepository;
import com.mongodb.kitchensink.util.Constants;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class UserValidation {

    private final UserInfoRepository userRepo;

    public UserValidation(UserInfoRepository userRepo) {
        this.userRepo = userRepo;
    }

    public void validateAdmin(Authentication authentication) throws AccessDeniedException {
        String jwtEmail = normEmail(authentication != null ? authentication.getName() : null);
        UserInfo actor = userRepo.findByEmail(jwtEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Actor not found: " + jwtEmail));
        if (!isAdmin(actor)) {
            throw new AccessDeniedException("You are not allowed to perform this action");
        }
    }

    public void validateAdminOrUserById(String id, Authentication authentication) {
        String jwtEmail = normEmail(authentication != null ? authentication.getName() : null);

        UserInfo user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserInfo actor = userRepo.findByEmail(jwtEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Actor not found: " + jwtEmail));

        boolean admin = isAdmin(actor);
        String targetEmail = normEmail(user.getEmail());
        if (!admin && !jwtEmail.equals(targetEmail)) {
            throw new AccessDeniedException("You are not allowed to perform this action");
        }
    }

    public void validateDeleteAdminOrUserById(String id, Authentication authentication) {
        String jwtEmail = normEmail(authentication != null ? authentication.getName() : null);

        UserInfo user = userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserInfo actor = userRepo.findByEmail(jwtEmail)
                .orElseThrow(() -> new UserNotFoundException("Actor not found: " + jwtEmail));

        boolean admin = isAdmin(actor);
        String targetEmail = normEmail(user.getEmail());
        if (!admin && !jwtEmail.equals(targetEmail)) {
            throw new AccessDeniedException("You are not allowed to delete another user's details");
        }
        if (isAdmin(user)) {
            throw new AccessDeniedException("Admin accounts cannot be deleted.");
        }
    }

    public boolean isExistingUser(UserRequest request) {
        String email = normEmail(request != null ? request.getEmail() : null);
        return email != null && userRepo.existsByEmail(email);
    }

    private boolean isAdmin(UserInfo u) {
        String roles = u != null && u.getRoles() != null ? u.getRoles() : "";
        return roles.contains(Constants.ROLES_ADMIN);
    }

    private String normEmail(String s) {
        if (s == null) return null;
        return s.trim().toLowerCase(Locale.ROOT);
    }
}