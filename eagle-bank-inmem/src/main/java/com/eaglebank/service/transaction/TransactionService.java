package com.eaglebank.service.transaction;

import com.eaglebank.bo.TransactionBO;
import com.eaglebank.dao.account.AccountDao;
import com.eaglebank.dao.transaction.TransactionDao;
import com.eaglebank.domain.AccountEntity;
import com.eaglebank.domain.TransactionEntity;
import com.eaglebank.exception.ForbiddenException;
import com.eaglebank.exception.InsufficientFundsException;
import com.eaglebank.exception.NotFoundException;
import com.eaglebank.mapper.transaction.TransactionEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

  private final TransactionDao transactionDao;
  private final AccountDao accountDao;
  private final TransactionEntityMapper transactionEntityMapper;


  /**
   * Create a transaction for the given user (deposit | withdrawal) and
   * update the account balance accordingly.
   * Returns the saved Transaction as a BO.
   */
  // Service
  public TransactionBO createTransaction(TransactionBO input)  {
    var account = accountDao.findByNumber(input.getAccountNumber())
            .orElseThrow(NotFoundException::new);
    assertOwnership(input.getUserId(), account);

    validateBusinessRules(input);

    // mutate account balance
    var newBalance = mutateBalance(account.getBalance(), input.getType(), input.getAmount());
    account.setBalance(newBalance);
    account.setUpdated(OffsetDateTime.now());
    accountDao.save(account);

    // build full entity with ALL fields set here
    var now = OffsetDateTime.now();
    var id  = generateTransactionId();

    var entity = TransactionEntity.builder()
            .id(id)
            .accountNumber(input.getAccountNumber())
            .userId(input.getUserId())
            .amount(input.getAmount())
            .currency(input.getCurrency())
            .type(input.getType())
            .reference(input.getReference())
            .created(now)
            .build();

    transactionDao.save(entity);

    // map back to BO so callers always get a fully-populated BO
    return transactionEntityMapper.toBO(entity);
  }


  /**
   * List transactions for an account owned by the user.
   */
  public List<TransactionBO> listTransactions(String userId, String accountNumber) {
    AccountEntity account = accountDao.findByNumber(accountNumber)
            .orElseThrow(NotFoundException::new);
    assertOwnership(userId, account);

    return transactionDao.findByAccountNumber(accountNumber).stream()
            .map(transactionEntityMapper::toBO)
            .collect(Collectors.toList());
  }

  /**
   * Fetch a single transaction for an account owned by the user.
   */
  public TransactionBO getTransactionForUser(String userId, String accountNumber, String transactionId) {
    AccountEntity account = accountDao.findByNumber(accountNumber)
            .orElseThrow(NotFoundException::new);
    assertOwnership(userId, account);

    TransactionEntity tx = transactionDao.findById(transactionId)
            .orElseThrow(NotFoundException::new);

    // extra guard: ensure the tx belongs to the same account
    if (!accountNumber.equals(tx.getAccountNumber())) {
      throw new ForbiddenException();
    }

    return transactionEntityMapper.toBO(tx);
  }

  /* --------------------- private helpers --------------------- */

  private void assertOwnership(String userId, AccountEntity account) {
    if (!userId.equals(account.getUserId())) {
      throw new ForbiddenException();
    }
  }

  private void validateBusinessRules(TransactionBO bo) {
    if (bo.getAmount() == null || bo.getAmount().signum() < 0) {
      throw new IllegalArgumentException("Amount must be non-negative");
    }
    if (bo.getCurrency() == null || !"GBP".equals(bo.getCurrency())) {
      throw new IllegalArgumentException("Unsupported currency");
    }
    if (bo.getType() == null || !(bo.getType().equals("deposit") || bo.getType().equals("withdrawal"))) {
      throw new IllegalArgumentException("Type must be 'deposit' or 'withdrawal'");
    }
  }

  private BigDecimal mutateBalance(BigDecimal current, String type, BigDecimal amount)  {
    if ("deposit".equals(type)) {
      return current.add(amount);
    }
    // withdrawal
    if (current.compareTo(amount) < 0) {
      throw new InsufficientFundsException("Insufficient funds");
    }
    return current.subtract(amount);
  }

  private String generateTransactionId() {
    // tan-<8 chars>
    return "tan-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
  }
}
