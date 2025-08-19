package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.dto.UserRequest;
import com.mongodb.kitchensink.dto.UserResponse;
import com.mongodb.kitchensink.dto.UserUpdateRequest;
import com.mongodb.kitchensink.exception.UserDeletionException;
import com.mongodb.kitchensink.exception.UserNotFoundException;
import com.mongodb.kitchensink.model.UserInfo;
import com.mongodb.kitchensink.repository.UserInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserInfoServiceImplTest {

    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    UserInfoRepository userRepo;
    @InjectMocks
    UserInfoServiceImpl service;

    private UserInfo sample;

    @BeforeEach
    void init() {
        sample = UserInfo.builder().id("id1").email("john@example.com").username("John").phone("9876543210").roles("ROLES_USER").password("enc").build();
    }

    @Test
    void createUser_saves_with_encoded_password_and_role() {
        when(passwordEncoder.encode("Plain@123")).thenReturn("ENCODED");
        when(userRepo.save(any())).thenAnswer(a -> {
            UserInfo u = a.getArgument(0);
            u.setId("X1");
            return u;
        });
        UserRequest req = new UserRequest();
        // name, email, phone, password setters exist via Lombok @Data
        req.setUsername(" John  ");
        req.setEmail("  JOHN@EXAMPLE.COM ");
        req.setPhone("(987)-654-3210");
        req.setPassword("Plain@123");

        service.createUser(req);

        ArgumentCaptor<UserInfo> cap = ArgumentCaptor.forClass(UserInfo.class);
        verify(userRepo).save(cap.capture());
        UserInfo saved = cap.getValue();
        assertEquals("john@example.com", saved.getEmail());
        assertEquals("9876543210", saved.getPhone());
        assertEquals("John", saved.getUsername());
        assertEquals("ENCODED", saved.getPassword());
        assertEquals("ROLES_USER", saved.getRoles());
    }

    @Test
    void getAllUsers_maps_entities_to_dto() {
        when(userRepo.findAll()).thenReturn(List.of(sample, UserInfo.builder().id("id2").email("a@b.com").username("A").phone("111").build()));
        List<UserResponse> out = service.getAllUsers();
        assertEquals(2, out.size());
        assertEquals("id1", out.get(0).getId());
        assertEquals("A", out.get(1).getName());
    }

    @Test
    void getAll_returns_page_of_responses() {
        Page<UserInfo> page = new PageImpl<>(List.of(sample));
        when(userRepo.findAll(PageRequest.of(0, 5))).thenReturn(page);
        Page<UserResponse> out = service.getAll(PageRequest.of(0, 5));
        assertEquals(1, out.getSize());
        assertEquals("id1", out.getContent().get(0).getId());
    }

    @Test
    void getById_present() {
        when(userRepo.findById("id1")).thenReturn(Optional.of(sample));
        UserResponse res = service.getById("id1");
        assertEquals("john@example.com", res.getEmail());
    }

    @Test
    void getById_absent_throws() {
        when(userRepo.findById("missing")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.getById("missing"));
    }

    @Test
    void getByEmail_present_and_absent() {
        when(userRepo.findByEmail("john@example.com")).thenReturn(Optional.of(sample));
        UserResponse ok = service.getByEmail("john@example.com");
        assertEquals("John", ok.getName());

        when(userRepo.findByEmail("no@x.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.getByEmail("no@x.com"));
    }

    @Test
    void getByEmailAndPhone_present_and_absent() {
        when(userRepo.findByEmailAndPhone("john@example.com", "9876543210")).thenReturn(Optional.of(sample));
        UserResponse ok = service.getByEmailAndPhone("  JOHN@example.com ", "(987) 654-3210");
        assertEquals("id1", ok.getId());

        when(userRepo.findByEmailAndPhone("no@x.com", "123")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.getByEmailAndPhone("no@x.com", "123"));
    }

    @Test
    void update_updates_fields_and_saves() {
        when(userRepo.findById("id1")).thenReturn(Optional.of(sample));
        when(userRepo.save(any())).thenAnswer(a -> a.getArgument(0));
        UserUpdateRequest req = new UserUpdateRequest();
        req.setEmail("New@Example.com");
        req.setPhone("999-888-7777");
        req.setName("New Name");
        UserResponse res = service.update("id1", req);
        assertEquals("john@example.com", res.getEmail());
        assertEquals("9998887777", res.getPhone());
        assertTrue(res.getName().startsWith("New"));
        verify(userRepo).save(any());
    }

    @Test
    void delete_when_present_deletes() {
        when(userRepo.findById("id1")).thenReturn(Optional.of(sample));
        service.delete("id1");
        verify(userRepo).deleteById("id1");
    }

    @Test
    void delete_when_absent_throws() {
        when(userRepo.findById("missing")).thenReturn(Optional.empty());
        assertThrows(UserDeletionException.class, () -> service.delete("missing"));
    }

    @Test
    void search_routes_to_correct_repo_calls() {
        Page<UserInfo> page = new PageImpl<>(List.of(sample));
        // both email & name
        when(userRepo.findByEmailContainingIgnoreCaseAndUsernameContainingIgnoreCase(eq("john@example.com"), eq("John"), any())).thenReturn(page);
        assertEquals(1, service.search("john@example.com", "John", null, null, PageRequest.of(0, 10)).getTotalElements());

        // only email
        when(userRepo.findByEmailContainingIgnoreCase(eq("john@example.com"), any())).thenReturn(page);
        assertEquals(1, service.search("john@example.com", null, null, null, PageRequest.of(0, 10)).getTotalElements());

        // only name
        when(userRepo.findByUsernameContainingIgnoreCase(eq("John"), any())).thenReturn(page);
        assertEquals(1, service.search(null, "John", null, null, PageRequest.of(0, 10)).getTotalElements());

        // neither -> findAll(pageable)
        when(userRepo.findAll(any(PageRequest.class))).thenReturn(page);
        assertEquals(1, service.search(" ", " ", null, null, PageRequest.of(0, 10)).getTotalElements());
    }
}
