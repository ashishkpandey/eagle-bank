package com.eaglebank.dao.transaction;

import com.eaglebank.domain.TransactionEntity;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for managing {@link TransactionEntity} persistence operations.
 * Provides CRUD-like access for transaction records.
 */
public interface TransactionDao {

  /**
   * Saves or updates a transaction record.
   *
   * @param transaction the transaction entity to persist
   */
  void save(TransactionEntity transaction);

  /**
   * Finds a transaction by its unique ID.
   *
   * @param transactionId the transaction ID
   * @return an {@link Optional} containing the transaction if found, otherwise empty
   */
  Optional<TransactionEntity> findById(String transactionId);

  /**
   * Retrieves all transactions associated with a specific account number.
   *
   * @param accountNumber the bank account number
   * @return list of transactions for the given account
   */
  List<TransactionEntity> findByAccountNumber(String accountNumber);
}
