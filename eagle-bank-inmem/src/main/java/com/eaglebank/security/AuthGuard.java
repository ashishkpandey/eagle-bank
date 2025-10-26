package com.eaglebank.security;

import com.eaglebank.domain.UserEntity;
import com.eaglebank.exception.NotAuthenticatedException;
import com.eaglebank.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthGuard {
    private final UserService userService;

    /** Returns the authenticated user's ID or throws 401 */
    public String requireUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new NotAuthenticatedException();
        }
        return userService.findByEmail(auth.getName())
                .map(UserEntity::getId)
                .orElseThrow(NotAuthenticatedException::new);
    }
}
