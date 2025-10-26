package com.eaglebank.security;

import com.eaglebank.domain.UserEntity;
import com.eaglebank.exception.NotAuthenticatedException;
import com.eaglebank.service.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthGuardTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requireUserId_returnsIdWhenAuthenticated() {
        UserService userService = mock(UserService.class);
        AuthGuard guard = new AuthGuard(userService);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("u@x.com", "pw"));
        SecurityContextHolder.setContext(context);

        UserEntity u = new UserEntity();
        u.setId("usr-1");
        when(userService.findByEmail("u@x.com")).thenReturn(Optional.of(u));

        assertEquals("usr-1", guard.requireUserId());
    }

    @Test
    void requireUserId_throwsWhenNoAuth() {
        UserService userService = mock(UserService.class);
        AuthGuard guard = new AuthGuard(userService);
        SecurityContextHolder.clearContext();
        assertThrows(NotAuthenticatedException.class, guard::requireUserId);
    }

    @Test
    void requireUserId_throwsWhenUnknownEmail() {
        UserService userService = mock(UserService.class);
        AuthGuard guard = new AuthGuard(userService);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("unknown@x", "pw"));
        SecurityContextHolder.setContext(context);

        when(userService.findByEmail("unknown@x")).thenReturn(Optional.empty());
        assertThrows(NotAuthenticatedException.class, guard::requireUserId);
    }
}