package com.eaglebank.mapper.account;

import com.eaglebank.bo.AccountBO;
import com.eaglebank.gen.model.BankAccountResponse;
import com.eaglebank.gen.model.CreateBankAccountRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountApiMapperTest {

    private final AccountApiMapper mapper = new AccountApiMapper();

    @Test
    void fromCreateRequest_mapsAll_andDefaults() {
        // given
        CreateBankAccountRequest req = new CreateBankAccountRequest()
                .name("Main")
                .accountType(CreateBankAccountRequest.AccountTypeEnum.fromValue("personal")); // explicit
        String userId = "usr-123";

        // when
        AccountBO bo = mapper.fromCreateRequest(req, userId);

        // then
        assertNotNull(bo, "BO should not be null");
        assertNotNull(bo.getAccountNumber(), "Account number should be generated");
        assertFalse(bo.getAccountNumber().isBlank(), "Account number should not be blank");

        assertEquals(userId, bo.getOwnerUserId());
        assertEquals("Main", bo.getName());
        assertEquals("personal", bo.getAccountType());
        assertEquals("10-10-10", bo.getSortCode());
        assertEquals("GBP", bo.getCurrency());
        assertEquals(BigDecimal.ZERO, bo.getBalance());

        assertNotNull(bo.getCreated(), "created timestamp should be set");
        assertNotNull(bo.getUpdated(), "updated timestamp should be set");
        assertFalse(bo.getCreated().isAfter(bo.getUpdated()), "created should be <= updated");
    }

    @Test
    void fromCreateRequest_defaultAccountTypeWhenNull() {
        // given
        CreateBankAccountRequest req = new CreateBankAccountRequest()
                .name("NoType"); // accountType intentionally left null
        String userId = "usr-xyz";

        // when
        AccountBO bo = mapper.fromCreateRequest(req, userId);

        // then
        assertEquals("personal", bo.getAccountType(), "Null accountType should default to 'personal'");
    }

    @Test
    void toResponse_mapsAllFieldsCorrectly() {
        // given
        OffsetDateTime now = OffsetDateTime.now();
        AccountBO bo = AccountBO.builder()
                .accountNumber("acc-001")
                .ownerUserId("usr-1")
                .sortCode("10-10-10")
                .name("Savings")
                .accountType("personal")
                .balance(new BigDecimal("1234.56"))
                .currency("GBP")
                .created(now.minusMinutes(1))
                .updated(now)
                .build();

        // when
        BankAccountResponse resp = mapper.toResponse(bo);

        // then
        assertNotNull(resp);
        assertEquals("acc-001", resp.getAccountNumber());

        assertNotNull(resp.getSortCode(), "sortCode enum should be set");
        assertEquals("10-10-10", resp.getSortCode().getValue());

        assertEquals("Savings", resp.getName());

        assertNotNull(resp.getAccountType(), "accountType enum should be set");
        assertEquals("personal", resp.getAccountType().getValue());

        // Balance is mapped to double; allow a tiny delta
        assertEquals(1234.56, resp.getBalance(), 0.000001);

        assertNotNull(resp.getCurrency(), "currency enum should be set");
        assertEquals("GBP", resp.getCurrency().getValue());

        assertEquals(bo.getCreated(), resp.getCreatedTimestamp());
        assertEquals(bo.getUpdated(), resp.getUpdatedTimestamp());
    }
}
