
package com.mongodb.kitchensink.config;

import com.mongodb.kitchensink.model.UserInfo;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UserInfoUserDetailsTest {

    @Test
    void maps_roles_and_exposes_flags() {
        UserInfo user = UserInfo.builder()
                .email("a@b.com").password("x").roles("ROLES_USER,ROLES_ADMIN").build();
        UserInfoUserDetails details = new UserInfoUserDetails(user);

        List<String> roles = details.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        assertTrue(roles.contains("ROLES_USER"));
        assertTrue(roles.contains("ROLES_ADMIN"));
        assertEquals("a@b.com", details.getUsername());
        assertEquals("x", details.getPassword());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());
    }
}
