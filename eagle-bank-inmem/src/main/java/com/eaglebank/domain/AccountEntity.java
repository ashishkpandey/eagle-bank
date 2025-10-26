package com.eaglebank.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Domain entity representing a bank account within the system.
 * This model is independent of persistence and API layers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {

  /** Unique 8-digit account number (e.g., "01234567") */
  private String accountNumber;

  /** Associated user ID (e.g., "usr-abc123") */
  private String userId;

  /** Sort code for the account (e.g., "10-10-10") */
  private String sortCode;

  /** Human-readable name for the account */
  private String name;

  /** Type of account (e.g., "personal", "business") */
  private String accountType;

  /** Current account balance */
  @Builder.Default
  private BigDecimal balance = BigDecimal.ZERO;

  /** ISO currency code (e.g., "GBP") */
  @Builder.Default
  private String currency = "GBP";

  /** Timestamp of creation */
  @Builder.Default
  private OffsetDateTime created = OffsetDateTime.now();

  /** Timestamp of last update */
  @Builder.Default
  private OffsetDateTime updated = OffsetDateTime.now();
}
