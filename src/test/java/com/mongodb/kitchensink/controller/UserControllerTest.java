
package com.mongodb.kitchensink.controller;

import com.mongodb.kitchensink.dto.UserResponse;
import com.mongodb.kitchensink.dto.UserUpdateRequest;
import com.mongodb.kitchensink.exception.InvalidFieldException;
import com.mongodb.kitchensink.service.UserInfoService;
import com.mongodb.kitchensink.service.UserValidation;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Test
    void healthCheck_returns_string() {
        UserController controller = new UserController(mock(UserInfoService.class), mock(UserValidation.class));
        assertEquals("API is working!", controller.healthCheck());
    }

    @Test
    void get_user_by_id_happy_path() {
        UserInfoService svc = mock(UserInfoService.class);
        UserValidation val = mock(UserValidation.class);
        when(svc.getById("id1")).thenReturn(new UserResponse("id1","A","a@b.com","1"));
        UserController c = new UserController(svc, val);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin@example.com");

        ResponseEntity<?> res = c.getUserDetails("id1", auth);
        assertEquals(200, res.getStatusCode().value());
        verify(val).validateAdminOrUserById(eq("id1"), eq(auth));
    }

    @Test
    void update_user_calls_service() {
        UserInfoService svc = mock(UserInfoService.class);
        UserValidation val = mock(UserValidation.class);
        UserController c = new UserController(svc, val);

        UserUpdateRequest req = new UserUpdateRequest();
        Authentication auth = mock(Authentication.class);
        when(svc.update(eq("id1"), any(UserUpdateRequest.class))).thenReturn(new UserResponse("id1","A","e","p"));

        ResponseEntity<?> res = c.updateUser("id1", req, auth);
        assertEquals(200, res.getStatusCode().value());
        verify(val).validateAdminOrUserById(eq("id1"), eq(auth));
    }

    @Test
    void delete_user_calls_service_and_returns_204() {
        UserInfoService svc = mock(UserInfoService.class);
        UserValidation val = mock(UserValidation.class);
        UserController c = new UserController(svc, val);
        Authentication auth = mock(Authentication.class);

        ResponseEntity<Void> res = c.deleteUser("id1", auth);
        assertEquals(204, res.getStatusCode().value());
        verify(svc).delete("id1");
        verify(val).validateDeleteAdminOrUserById(eq("id1"), eq(auth));
    }

    @Test
    void me_returns_current_user_by_email() {
        UserInfoService svc = mock(UserInfoService.class);
        UserValidation val = mock(UserValidation.class);
        UserController c = new UserController(svc, val);
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");
        when(svc.getByEmail("user@example.com")).thenReturn(new UserResponse("id","U","user@example.com","p"));

        ResponseEntity<?> res = c.getCurrentUser(auth);
        assertEquals(200, res.getStatusCode().value());
        verify(svc).getByEmail("user@example.com");
    }

    @Test
    void search_requires_at_least_one_param() {
        UserInfoService svc = mock(UserInfoService.class);
        UserValidation val = mock(UserValidation.class);
        UserController c = new UserController(svc, val);
        Authentication auth = mock(Authentication.class);

        assertThrows(InvalidFieldException.class, () ->
                c.searchUsers(" "," ", PageRequest.of(0,10), auth));
    }

    @Test
    void search_with_email_and_name_calls_service_and_validates_admin() {
        UserInfoService svc = mock(UserInfoService.class);
        UserValidation val = mock(UserValidation.class);
        UserController c = new UserController(svc, val);
        Authentication auth = mock(Authentication.class);
        Page<UserResponse> page = new PageImpl<>(List.of(new UserResponse("id","A","e","p")));
        when(svc.search(eq("a@b.com"), eq("Alice"), any())).thenReturn(page);

        ResponseEntity<?> res = c.searchUsers("a@b.com", "Alice", PageRequest.of(0,10), auth);
        assertEquals(200, res.getStatusCode().value());
        verify(val).validateAdmin(auth);
    }

//    @Test
//    void getAll_and_getAllUsers_calls_validation_and_service() {
//        UserInfoService svc = mock(UserInfoService.class);
//        UserValidation val = mock(UserValidation.class);
//        UserController c = new UserController(svc, val);
//        Authentication auth = mock(Authentication.class);
//
//        when(svc.getAll(PageRequest.of(0,10))).thenReturn(new PageImpl<>(List.of(new UserResponse("id","n","e","p"))));
//        ResponseEntity<?> p = c.getAllUsers(PageRequest.of(0,10),null,null, auth);
//        assertEquals(200, p.getStatusCode().value());
//        verify(val).validateAdmin(auth);
//
//        when(svc.getAllUsers()).thenReturn(List.of(new UserResponse("id","n","e","p")));
//        ResponseEntity<?> all = c.getAllUsers(auth);
//        assertEquals(200, all.getStatusCode().value());
//        verify(val, times(2)).validateAdmin(auth);
//    }
}
