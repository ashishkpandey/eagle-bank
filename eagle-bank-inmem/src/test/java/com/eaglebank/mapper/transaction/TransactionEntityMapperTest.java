package com.eaglebank.mapper.transaction;

import com.eaglebank.bo.TransactionBO;
import com.eaglebank.domain.TransactionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionEntityMapperTest {

    private TransactionEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TransactionEntityMapper();
    }

    @Test
    void toEntity_mapsAllFieldsCorrectly() {
        OffsetDateTime now = OffsetDateTime.now();

        TransactionBO bo = TransactionBO.builder()
                .id("txn-001")
                .accountNumber("acc-001")
                .userId("usr-001")
                .amount(new BigDecimal("500.25"))
                .currency("GBP")
                .type("deposit")
                .reference("Test deposit")
                .created(now)
                .build();

        TransactionEntity entity = mapper.toEntity(bo);

        assertNotNull(entity);
        assertEquals(bo.getId(), entity.getId());
        assertEquals(bo.getAccountNumber(), entity.getAccountNumber());
        assertEquals(bo.getUserId(), entity.getUserId());
        assertEquals(bo.getAmount(), entity.getAmount());
        assertEquals(bo.getCurrency(), entity.getCurrency());
        assertEquals(bo.getType(), entity.getType());
        assertEquals(bo.getReference(), entity.getReference());
        assertEquals(bo.getCreated(), entity.getCreated());
    }

    @Test
    void toBO_mapsAllFieldsCorrectly() {
        OffsetDateTime now = OffsetDateTime.now();

        TransactionEntity entity = TransactionEntity.builder()
                .id("txn-999")
                .accountNumber("acc-999")
                .userId("usr-999")
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .type("withdrawal")
                .reference("Test withdrawal")
                .created(now.minusMinutes(2))
                .build();

        TransactionBO bo = mapper.toBO(entity);

        assertNotNull(bo);
        assertEquals(entity.getId(), bo.getId());
        assertEquals(entity.getAccountNumber(), bo.getAccountNumber());
        assertEquals(entity.getUserId(), bo.getUserId());
        assertEquals(entity.getAmount(), bo.getAmount());
        assertEquals(entity.getCurrency(), bo.getCurrency());
        assertEquals(entity.getType(), bo.getType());
        assertEquals(entity.getReference(), bo.getReference());
        assertEquals(entity.getCreated(), bo.getCreated());
    }

    @Test
    void toEntityAndBack_roundTripKeepsValues() {
        OffsetDateTime now = OffsetDateTime.now();

        TransactionBO original = TransactionBO.builder()
                .id("txn-roundtrip")
                .accountNumber("acc-rtp")
                .userId("usr-rtp")
                .amount(new BigDecimal("200.00"))
                .currency("EUR")
                .type("deposit")
                .reference("RoundTrip")
                .created(now)
                .build();

        TransactionEntity entity = mapper.toEntity(original);
        TransactionBO result = mapper.toBO(entity);

        assertEquals(original.getId(), result.getId());
        assertEquals(original.getAccountNumber(), result.getAccountNumber());
        assertEquals(original.getUserId(), result.getUserId());
        assertEquals(original.getAmount(), result.getAmount());
        assertEquals(original.getCurrency(), result.getCurrency());
        assertEquals(original.getType(), result.getType());
        assertEquals(original.getReference(), result.getReference());
        assertEquals(original.getCreated(), result.getCreated());
    }
}
