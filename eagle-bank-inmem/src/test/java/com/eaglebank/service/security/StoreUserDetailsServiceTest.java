package com.eaglebank.service.security;

import com.eaglebank.domain.UserEntity;
import com.eaglebank.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreUserDetailsServiceTest {

    private UserService userService;
    private StoreUserDetailsService storeUserDetailsService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        storeUserDetailsService = new StoreUserDetailsService(userService);
    }

    @Test
    void loadUserByUsername_returnsUserDetails_whenUserExists() {
        // given
        UserEntity user = new UserEntity();
        user.setId("usr-123");
        user.setEmail("test@example.com");
        user.setPasswordHash("$2a$10$hashedPassword");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // when
        UserDetails details = storeUserDetailsService.loadUserByUsername("test@example.com");

        // then
        assertNotNull(details);
        assertEquals("test@example.com", details.getUsername());
        assertEquals("$2a$10$hashedPassword", details.getPassword());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_throwsException_whenUserNotFound() {
        when(userService.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> storeUserDetailsService.loadUserByUsername("missing@example.com")
        );

        assertTrue(ex.getMessage().contains("missing@example.com"));
    }
}
