package com.eaglebank.service.user;

import com.eaglebank.bo.UserBO;
import com.eaglebank.dao.user.UserDao;
import com.eaglebank.domain.UserEntity;
import com.eaglebank.exception.ForbiddenException;
import com.eaglebank.exception.NotFoundException;
import com.eaglebank.mapper.user.UserEntityMapper;
import com.eaglebank.testutil.MockData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserDao dao;
    private PasswordEncoder encoder;
    private UserEntityMapper mapper;
    private UserService svc;

    @BeforeEach
    void setup() {
        dao = mock(UserDao.class);
        encoder = mock(PasswordEncoder.class);
        mapper = new UserEntityMapper();
        // matches your @RequiredArgsConstructor(UserDao, PasswordEncoder, UserEntityMapper)
        svc = new UserService(dao, encoder, mapper);
    }

    @Test
    void createUser_encodesPassword_and_saves() {
        UserBO input = MockData.userBO("u1", "Ashish Pandey", "a@b.com", "secret");
        when(encoder.encode("secret")).thenReturn("HASH");

        UserBO out = svc.createUser(input);

        verify(dao).save(argThat(e ->
                "u1".equals(e.getId()) &&
                        "a@b.com".equals(e.getEmail()) &&
                        "Ashish Pandey".equals(e.getName()) &&
                        "HASH".equals(e.getPasswordHash())
        ));

        assertEquals("u1", out.getId());
        assertEquals("Ashish Pandey", out.getName());
        assertEquals("a@b.com", out.getEmail());
    }

    @Test
    void getUserForRequester_happyPath() {
        UserEntity e = MockData.userEntity("u1", "a@b.com", "Ashish Pandey");
        when(dao.findById("u1")).thenReturn(Optional.of(e));

        UserBO bo = svc.getUserForRequester("u1", "u1");

        assertEquals("u1", bo.getId());
        assertEquals("Ashish Pandey", bo.getName());
        assertEquals("a@b.com", bo.getEmail());
    }

    @Test
    void getUserForRequester_404() {
        when(dao.findById("x")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> svc.getUserForRequester("u1", "x"));
    }

    @Test
    void getUserForRequester_403_ifDifferentUser() {
        UserEntity e = MockData.userEntity("u2", "b@c.com", "Other User");
        when(dao.findById("u2")).thenReturn(Optional.of(e));
        assertThrows(ForbiddenException.class, () -> svc.getUserForRequester("u1", "u2"));
    }
}
