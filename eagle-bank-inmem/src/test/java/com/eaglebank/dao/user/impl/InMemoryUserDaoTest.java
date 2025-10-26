package com.eaglebank.dao.user.impl;

import com.eaglebank.domain.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserDaoTest {

    private InMemoryUserDao dao;

    @BeforeEach
    void setup() {
        dao = new InMemoryUserDao();
    }

    private UserEntity user(String id, String email, String name) {
        UserEntity u = new UserEntity();
        u.setId(id);
        u.setEmail(email);
        u.setName(name);
        u.setCreated(OffsetDateTime.now());
        u.setUpdated(OffsetDateTime.now());
        return u;
    }

    @Test
    void save_and_findById_and_findByEmail() {
        UserEntity u = user("u1", "a@b.com", "Ashish Pandey");
        dao.save(u);

        Optional<UserEntity> byId = dao.findById("u1");
        Optional<UserEntity> byEmail = dao.findByEmail("a@b.com");

        assertTrue(byId.isPresent());
        assertTrue(byEmail.isPresent());
        assertEquals("u1", byEmail.get().getId());
        assertEquals("Ashish Pandey", byId.get().getName());
    }

    @Test
    void findById_returnsEmptyIfNotFound() {
        assertTrue(dao.findById("missing").isEmpty());
    }

    @Test
    void findByEmail_returnsEmptyIfNotFound() {
        assertTrue(dao.findByEmail("none@x.com").isEmpty());
    }

    @Test
    void save_overwritesExistingUser() {
        UserEntity u1 = user("u1", "a@b.com", "Original");
        dao.save(u1);

        UserEntity u2 = user("u1", "a@b.com", "Updated");
        dao.save(u2);

        Optional<UserEntity> found = dao.findById("u1");
        assertTrue(found.isPresent());
        assertEquals("Updated", found.get().getName());
    }

    @Test
    void save_throwsIfMissingIdOrEmail() {
        UserEntity invalid = new UserEntity();
        assertThrows(IllegalArgumentException.class, () -> dao.save(invalid));

        UserEntity u1 = new UserEntity();
        u1.setId("u1");
        assertThrows(IllegalArgumentException.class, () -> dao.save(u1));

        UserEntity u2 = new UserEntity();
        u2.setEmail("a@b.com");
        assertThrows(IllegalArgumentException.class, () -> dao.save(u2));
    }
}
