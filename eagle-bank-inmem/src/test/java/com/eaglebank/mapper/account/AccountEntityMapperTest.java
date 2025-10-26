package com.eaglebank.mapper.account;

import com.eaglebank.bo.AccountBO;
import com.eaglebank.domain.AccountEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountEntityMapperTest {

    private AccountEntityMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AccountEntityMapper();
    }

    @Test
    void toEntity_mapsAllFieldsCorrectly() {
        OffsetDateTime now = OffsetDateTime.now();

        AccountBO bo = AccountBO.builder()
                .accountNumber("ACC-001")
                .ownerUserId("USR-001")
                .sortCode("10-20-30")
                .name("Savings")
                .accountType("personal")
                .balance(new BigDecimal("1000.50"))
                .currency("GBP")
                .created(now.minusDays(1))
                .updated(now)
                .build();

        AccountEntity entity = mapper.toEntity(bo);

        assertNotNull(entity);
        assertEquals(bo.getAccountNumber(), entity.getAccountNumber());
        assertEquals(bo.getOwnerUserId(), entity.getUserId());
        assertEquals(bo.getSortCode(), entity.getSortCode());
        assertEquals(bo.getName(), entity.getName());
        assertEquals(bo.getAccountType(), entity.getAccountType());
        assertEquals(bo.getBalance(), entity.getBalance());
        assertEquals(bo.getCurrency(), entity.getCurrency());
        assertEquals(bo.getCreated(), entity.getCreated());
        assertEquals(bo.getUpdated(), entity.getUpdated());
    }

    @Test
    void toBO_mapsAllFieldsCorrectly() {
        OffsetDateTime now = OffsetDateTime.now();

        AccountEntity entity = AccountEntity.builder()
                .accountNumber("ACC-999")
                .userId("USR-999")
                .sortCode("40-50-60")
                .name("Current")
                .accountType("business")
                .balance(new BigDecimal("250.75"))
                .currency("USD")
                .created(now.minusHours(2))
                .updated(now)
                .build();

        AccountBO bo = mapper.toBO(entity);

        assertNotNull(bo);
        assertEquals(entity.getAccountNumber(), bo.getAccountNumber());
        assertEquals(entity.getUserId(), bo.getOwnerUserId());
        assertEquals(entity.getSortCode(), bo.getSortCode());
        assertEquals(entity.getName(), bo.getName());
        assertEquals(entity.getAccountType(), bo.getAccountType());
        assertEquals(entity.getBalance(), bo.getBalance());
        assertEquals(entity.getCurrency(), bo.getCurrency());
        assertEquals(entity.getCreated(), bo.getCreated());
        assertEquals(entity.getUpdated(), bo.getUpdated());
    }

    @Test
    void toEntityAndBack_roundTripKeepsValues() {
        OffsetDateTime now = OffsetDateTime.now();

        AccountBO original = AccountBO.builder()
                .accountNumber("ACC-777")
                .ownerUserId("USR-777")
                .sortCode("99-88-77")
                .name("RoundTrip")
                .accountType("personal")
                .balance(BigDecimal.TEN)
                .currency("GBP")
                .created(now.minusMinutes(5))
                .updated(now)
                .build();

        AccountEntity entity = mapper.toEntity(original);
        AccountBO roundTrip = mapper.toBO(entity);

        assertEquals(original.getAccountNumber(), roundTrip.getAccountNumber());
        assertEquals(original.getOwnerUserId(), roundTrip.getOwnerUserId());
        assertEquals(original.getSortCode(), roundTrip.getSortCode());
        assertEquals(original.getName(), roundTrip.getName());
        assertEquals(original.getAccountType(), roundTrip.getAccountType());
        assertEquals(original.getBalance(), roundTrip.getBalance());
        assertEquals(original.getCurrency(), roundTrip.getCurrency());
        assertEquals(original.getCreated(), roundTrip.getCreated());
        assertEquals(original.getUpdated(), roundTrip.getUpdated());
    }
}
