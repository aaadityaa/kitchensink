
package com.mongodb.kitchensink.controller;

import com.mongodb.kitchensink.dto.AuthRequest;
import com.mongodb.kitchensink.dto.UserRequest;
import com.mongodb.kitchensink.exception.UserCreationException;
import com.mongodb.kitchensink.service.AuthService;
import com.mongodb.kitchensink.service.UserInfoService;
import com.mongodb.kitchensink.service.UserValidation;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Test
    void signup_creates_user_and_returns_201() {
        UserInfoService svc = mock(UserInfoService.class);
        UserValidation validation = mock(UserValidation.class);
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(svc, validation, authService);

        UserRequest req = new UserRequest();
        req.setUsername("John");
        req.setEmail("john@example.com");
        req.setPhone("9876543210");
        req.setPassword("P@ssw0rd");

        ResponseEntity<?> res = controller.register(req);
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        verify(svc).createUser(any(UserRequest.class));
    }

    @Test
    void signup_existing_user_throws() {
        UserInfoService svc = mock(UserInfoService.class);
        UserValidation validation = mock(UserValidation.class);
        when(validation.isExistingUser(any())).thenReturn(true);
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(svc, validation, authService);

        UserRequest req = new UserRequest();
        assertThrows(UserCreationException.class, () -> controller.register(req));
    }

    @Test
    void login_returns_token_in_body() {
        UserInfoService svc = mock(UserInfoService.class);
        UserValidation validation = mock(UserValidation.class);
        AuthService authService = mock(AuthService.class);
        when(authService.login(any(AuthRequest.class))).thenReturn("TOKEN");
        AuthController controller = new AuthController(svc, validation, authService);

        AuthRequest req = new AuthRequest();
        req.setEmail("john@example.com"); req.setPassword("x");
        ResponseEntity<Map<String,String>> res = controller.login(req);
        assertEquals(200, res.getStatusCode().value());
        assertEquals("TOKEN", res.getBody().get("token"));
        assertEquals("Bearer", res.getBody().get("type"));
    }
}
