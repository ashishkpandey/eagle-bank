package com.eaglebank.dao.transaction.impl;

import com.eaglebank.dao.transaction.TransactionDao;
import com.eaglebank.domain.TransactionEntity;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory implementation of {@link TransactionDao}.
 * Stores transactions in maps indexed by transaction ID and account number.
 */
@Repository
public class InMemoryTransactionDao implements TransactionDao {

  /** Primary storage: transaction ID → TransactionEntity */
  private final Map<String, TransactionEntity> transactionsById = new ConcurrentHashMap<>();

  /** Secondary index: account number → list of transaction IDs */
  private final Map<String, List<String>> transactionIdsByAccount = new ConcurrentHashMap<>();

  @Override
  public void save(TransactionEntity transaction) {
    Objects.requireNonNull(transaction, "Transaction must not be null");
    Objects.requireNonNull(transaction.getId(), "Transaction ID must not be null");
    Objects.requireNonNull(transaction.getAccountNumber(), "Account number must not be null");

    transactionsById.put(transaction.getId(), transaction);

    // Ensure insertion at the head of the list (most recent first)
    transactionIdsByAccount
            .computeIfAbsent(transaction.getAccountNumber(), k -> Collections.synchronizedList(new LinkedList<>()))
            .add(0, transaction.getId());
  }

  @Override
  public Optional<TransactionEntity> findById(String transactionId) {
    return Optional.ofNullable(transactionsById.get(transactionId));
  }

  @Override
  public List<TransactionEntity> findByAccountNumber(String accountNumber) {
    return transactionIdsByAccount
            .getOrDefault(accountNumber, List.of())
            .stream()
            .map(transactionsById::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableList());
  }
}
