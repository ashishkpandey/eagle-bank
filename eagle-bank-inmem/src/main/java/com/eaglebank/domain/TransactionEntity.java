package com.eaglebank.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Domain entity representing a financial transaction
 * recorded against a specific bank account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {

  /** Unique transaction ID (e.g., "txn-abc123") */
  private String id;

  /** Associated account number for this transaction */
  private String accountNumber;

  /** User ID who owns this transaction */
  private String userId;

  /** Transaction amount (positive for deposits, negative for withdrawals) */
  private BigDecimal amount;

  /** ISO currency code (e.g., "GBP") */
  @Builder.Default
  private String currency = "GBP";

  /** Type of transaction — deposit or withdrawal */
  private String type;

  /** Optional reference or remark for the transaction */
  private String reference;

  /** Timestamp of when this transaction was created */
  @Builder.Default
  private OffsetDateTime created = OffsetDateTime.now();
}
