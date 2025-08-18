
package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.dto.AuthRequest;
import com.mongodb.kitchensink.model.UserInfo;
import com.mongodb.kitchensink.repository.UserInfoRepository;
import com.mongodb.kitchensink.util.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @Mock UserInfoRepository userRepo;
    @InjectMocks AuthService authService;

    @Test
    void login_when_authenticated_returns_token() {
        AuthRequest req = new AuthRequest();
        req.setEmail("john@example.com");
        req.setPassword("pw");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(UserInfo.builder().email("john@example.com").roles("ROLES_ADMIN").build()));
        when(jwtService.generateToken("john@example.com","ROLES_ADMIN")).thenReturn("TOKEN");

        String token = authService.login(req);
        assertEquals("TOKEN", token);
    }

    @Test
    void login_when_not_authenticated_returns_empty() {
        AuthRequest req = new AuthRequest();
        req.setEmail("john@example.com");
        req.setPassword("pw");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(false);

        String token = authService.login(req);
        assertEquals(Constants.STRING_EMPTY, token);
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }
}
