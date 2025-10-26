package com.eaglebank.dao.account.impl;

import com.eaglebank.dao.account.AccountDao;
import com.eaglebank.domain.AccountEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of {@link AccountDao}.
 * Stores AccountEntity objects in a thread-safe map keyed by account number.
 */
@Repository
public class InMemoryAccountDao implements AccountDao {

  /** Thread-safe storage for accounts keyed by account number */
  private final Map<String, AccountEntity> accountsByNumber = new ConcurrentHashMap<>();

  @Override
  public void save(AccountEntity account) {
    accountsByNumber.put(account.getAccountNumber(), account);
  }

  @Override
  public Optional<AccountEntity> findByNumber(String accountNumber) {
    return Optional.ofNullable(accountsByNumber.get(accountNumber));
  }

  @Override
  public List<AccountEntity> findByUserId(String userId) {
    return accountsByNumber.values().stream()
            .filter(account -> userId.equals(account.getUserId()))
            .collect(Collectors.toList());
  }

  @Override
  public void deleteByNumber(String accountNumber) {
    accountsByNumber.remove(accountNumber);
  }
}
