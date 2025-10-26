package com.eaglebank.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountBO {
    private String accountNumber;
    private String ownerUserId;
    private String sortCode;
    private String name;
    private String accountType;   // e.g. "personal"
    private BigDecimal balance;
    private String currency;      // e.g. "GBP"
    private OffsetDateTime created;
    private OffsetDateTime updated;
}
