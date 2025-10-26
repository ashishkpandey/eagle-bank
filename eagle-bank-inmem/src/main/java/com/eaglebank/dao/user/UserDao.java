package com.eaglebank.dao.user;

import com.eaglebank.domain.UserEntity;

import java.util.Optional;

/**
 * Data Access Object for managing {@link UserEntity} persistence operations.
 * Provides methods for saving and retrieving user data.
 */
public interface UserDao {

  /**
   * Saves or updates a user record.
   *
   * @param user the user entity to persist
   */
  void save(UserEntity user);

  /**
   * Finds a user by their unique ID.
   *
   * @param userId the ID of the user
   * @return an {@link Optional} containing the user if found, otherwise empty
   */
  Optional<UserEntity> findById(String userId);

  /**
   * Finds a user by their email address.
   *
   * @param email the email of the user
   * @return an {@link Optional} containing the user if found, otherwise empty
   */
  Optional<UserEntity> findByEmail(String email);
}
