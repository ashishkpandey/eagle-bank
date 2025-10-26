package com.eaglebank.service.account;

import com.eaglebank.bo.AccountBO;
import com.eaglebank.dao.account.AccountDao;
import com.eaglebank.domain.AccountEntity;
import com.eaglebank.exception.ForbiddenException;
import com.eaglebank.exception.NotFoundException;
import com.eaglebank.mapper.account.AccountEntityMapper;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AccountService {

  private final AccountDao accountDao;
  private final AccountEntityMapper accountEntityMapper;

  public AccountService(AccountDao accountDao, AccountEntityMapper accountEntityMapper) {
    this.accountDao = accountDao;
    this.accountEntityMapper = accountEntityMapper;
  }

  /** Create and persist a new account (BO at boundary). */
  public AccountBO createAccount(AccountBO account) {
    AccountEntity entity = accountEntityMapper.toEntity(account);
    accountDao.save(entity);
    return accountEntityMapper.toBO(entity);
  }

  /** Read one account for owner (ownership verified). */
  public AccountBO getAccountForUser(String userId, String accountNumber) {
    AccountEntity entity = accountDao.findByNumber(accountNumber)
            .orElseThrow(NotFoundException::new);
    assertOwnership(userId, entity);
    return accountEntityMapper.toBO(entity);
  }

  /** Rename an owned account; returns updated BO. */
  public AccountBO updateAccountName(String userId, String accountNumber, String newName) {
    AccountEntity entity = accountDao.findByNumber(accountNumber)
            .orElseThrow(NotFoundException::new);
    assertOwnership(userId, entity);

    if (newName != null && !newName.isBlank()) {
      entity.setName(newName);
      entity.setUpdated(OffsetDateTime.now());
      accountDao.save(entity);
    }
    return accountEntityMapper.toBO(entity);
  }

  /** Delete an owned account. */
  public void deleteAccount(String userId, String accountNumber) {
    AccountEntity entity = accountDao.findByNumber(accountNumber)
            .orElseThrow(NotFoundException::new);
    assertOwnership(userId, entity);
    accountDao.deleteByNumber(entity.getAccountNumber());
  }

  /** List all accounts owned by user. */
  public List<AccountBO> listUserAccounts(String userId) {
    return accountDao.findByUserId(userId).stream()
            .map(accountEntityMapper::toBO)
            .toList();
  }

  /** Ensure the caller owns the account. */
  private void assertOwnership(String userId, AccountEntity entity) {
    if (!userId.equals(entity.getUserId())) {
      throw new ForbiddenException();
    }
  }
}
