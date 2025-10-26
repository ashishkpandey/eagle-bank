package com.eaglebank.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionBO {
    private String id;
    private String accountNumber;
    private String userId;
    private BigDecimal amount;
    private String currency;    // "GBP"
    private String type;        // "deposit" | "withdrawal"
    private String reference;
    private OffsetDateTime created;
}
