package com.eaglebank.dao.user.impl;

import com.eaglebank.dao.user.UserDao;
import com.eaglebank.domain.UserEntity;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of {@link UserDao}.
 * Maintains user data indexed by both user ID and email address.
 */
@Repository
public class InMemoryUserDao implements UserDao {

  /** Primary storage: user ID → UserEntity */
  private final Map<String, UserEntity> usersById = new ConcurrentHashMap<>();

  /** Secondary index: email → user ID */
  private final Map<String, String> userIdByEmail = new ConcurrentHashMap<>();

  @Override
  public void save(UserEntity user) {
    if (user == null || user.getId() == null || user.getEmail() == null) {
      throw new IllegalArgumentException("User, ID, and email must not be null");
    }

    usersById.put(user.getId(), user);
    userIdByEmail.put(user.getEmail(), user.getId());
  }

  @Override
  public Optional<UserEntity> findById(String userId) {
    return Optional.ofNullable(usersById.get(userId));
  }

  @Override
  public Optional<UserEntity> findByEmail(String email) {
    String id = userIdByEmail.get(email);
    return Optional.ofNullable(id).map(usersById::get);
  }
}
