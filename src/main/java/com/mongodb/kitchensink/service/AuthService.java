package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.dto.AuthRequest;
import com.mongodb.kitchensink.model.UserInfo;
import com.mongodb.kitchensink.repository.UserInfoRepository;
import com.mongodb.kitchensink.util.Constants;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserInfoRepository userRepo;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, UserInfoRepository userRepo) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepo = userRepo;
    }

    public String login(AuthRequest authRequest) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            UserInfo user = userRepo.findByEmail(authRequest.getEmail()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return jwtService.generateToken(authRequest.getEmail(), user.getRoles());
        }
        return Constants.STRING_EMPTY;
    }

}