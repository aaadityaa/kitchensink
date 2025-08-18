
package com.mongodb.kitchensink.config;

import com.mongodb.kitchensink.model.UserInfo;
import com.mongodb.kitchensink.repository.UserInfoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserInfoUserDetailsServiceTest {

    @Mock UserInfoRepository repo;
    @InjectMocks UserInfoUserDetailsService svc;

    @Test
    void load_user_ok() {
        when(repo.findByEmail("x@y.com")).thenReturn(Optional.of(UserInfo.builder().email("x@y.com").password("p").roles("ROLES_USER").build()));
        assertEquals("x@y.com", svc.loadUserByUsername("x@y.com").getUsername());
    }

    @Test
    void load_user_missing_throws() {
        when(repo.findByEmail("none")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> svc.loadUserByUsername("none"));
    }
}
