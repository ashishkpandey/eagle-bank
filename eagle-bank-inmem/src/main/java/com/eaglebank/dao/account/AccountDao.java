package com.eaglebank.dao.account;

import com.eaglebank.domain.AccountEntity;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for managing {@link AccountEntity} persistence operations.
 * Implementations may use in-memory, database, or other storage mechanisms.
 */
public interface AccountDao {

  /**
   * Saves or updates an account record.
   *
   * @param account the account entity to persist
   */
  void save(AccountEntity account);

  /**
   * Finds an account by its unique account number.
   *
   * @param accountNumber the account number to search for
   * @return an {@link Optional} containing the account if found, otherwise empty
   */
  Optional<AccountEntity> findByNumber(String accountNumber);

  /**
   * Retrieves all accounts owned by a specific user.
   *
   * @param userId the user ID whose accounts should be listed
   * @return list of accounts belonging to the user
   */
  List<AccountEntity> findByUserId(String userId);

  /**
   * Deletes an account identified by its account number.
   *
   * @param accountNumber the account number to delete
   */
  void deleteByNumber(String accountNumber);
}
