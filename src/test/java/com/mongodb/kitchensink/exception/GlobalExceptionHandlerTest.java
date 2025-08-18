
package com.mongodb.kitchensink.exception;

import com.mongodb.kitchensink.util.Constants;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleUnreadable_returns_400() {
        ResponseEntity<Map<String,Object>> res = handler.handleUnreadable(new HttpMessageNotReadableException("bad"));
        assertEquals(400, res.getStatusCode().value());
        assertEquals(Constants.BAD_REQUEST, res.getBody().get(Constants.ERROR));
    }

    @Test
    void handleAuth_returns_401() {
        ResponseEntity<Map<String,Object>> res = handler.handleAuth(new BadCredentialsException("nope"));
        assertEquals(401, res.getStatusCode().value());
        assertEquals(Constants.UNAUTHORIZED, res.getBody().get(Constants.ERROR));
    }

    @Test
    void handleDenied_returns_403() {
        ResponseEntity<Map<String,Object>> res = handler.handleDenied(new AccessDeniedException("denied"));
        assertEquals(403, res.getStatusCode().value());
        assertEquals(Constants.FORBIDDEN, res.getBody().get(Constants.ERROR));
    }

    @Test
    void handleNotFound_returns_404() {
        ResponseEntity<Map<String,Object>> res = handler.handleNotFound(new NoSuchElementException("nf"));
        assertEquals(404, res.getStatusCode().value());
        assertEquals(Constants.NOT_FOUND, res.getBody().get(Constants.ERROR));
    }

    @Test
    void handleDuplicate_returns_409() {
        ResponseEntity<Map<String,Object>> res = handler.handleConflict(new DataIntegrityViolationException("dup"));
        assertEquals(409, res.getStatusCode().value());
        assertEquals(Constants.CONFLICT, res.getBody().get(Constants.ERROR));
    }

    @Test
    void handleGeneric_returns_500() {
        ResponseEntity<Map<String,Object>> res = handler.handleGeneric(new RuntimeException("boom"));
        assertEquals(500, res.getStatusCode().value());
        assertEquals(Constants.INTERNAL_SERVER_ERROR, res.getBody().get(Constants.ERROR));
    }
}
