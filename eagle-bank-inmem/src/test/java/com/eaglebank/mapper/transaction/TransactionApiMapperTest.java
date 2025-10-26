package com.eaglebank.mapper.transaction;

import com.eaglebank.bo.TransactionBO;
import com.eaglebank.gen.model.CreateTransactionRequest;
import com.eaglebank.gen.model.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionApiMapperTest {

    private TransactionApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TransactionApiMapper();
    }

    @Test
    void fromRequest_mapsAllFields() {
        // given
        CreateTransactionRequest req = new CreateTransactionRequest()
                .amount(12.34)
                .type(CreateTransactionRequest.TypeEnum.fromValue("withdrawal"))
                .reference("Rent payment");
        String accountNumber = "acc-123";
        String userId = "usr-999";

        // when
        TransactionBO bo = mapper.fromRequest(req, accountNumber, userId);

        // then
        assertNotNull(bo.getId(), "Id should be generated");
        assertEquals(accountNumber, bo.getAccountNumber());
        assertEquals(userId, bo.getUserId());
        assertEquals(new BigDecimal("12.34"), bo.getAmount());
        assertEquals("GBP", bo.getCurrency());
        assertEquals("withdrawal", bo.getType());
        assertEquals("Rent payment", bo.getReference());
        assertNotNull(bo.getCreated(), "Created timestamp should be set");
    }

    @Test
    void fromRequest_defaultsWhenNulls() {
        // given: no amount, no currency, no type
        CreateTransactionRequest req = new CreateTransactionRequest()
                .reference("Top up");
        // when
        TransactionBO bo = mapper.fromRequest(req, "acc-1", "usr-1");
        // then
        assertEquals(BigDecimal.ZERO, bo.getAmount(), "Default amount should be ZERO");
        assertEquals("GBP", bo.getCurrency(), "Default currency should be GBP");
        assertEquals("deposit", bo.getType(), "Default type should be deposit");
        assertEquals("Top up", bo.getReference());
    }

    @Test
    void toResponse_mapsAllFieldsCorrectly() {
        // given
        OffsetDateTime now = OffsetDateTime.now();
        TransactionBO bo = TransactionBO.builder()
                .id("tan-abc12345")
                .accountNumber("acc-9")     // not exposed in response per mapper
                .userId("usr-9")
                .amount(new BigDecimal("99.50"))
                .currency("GBP")
                .type("deposit")
                .reference("Salary")
                .created(now)
                .build();

        // when
        TransactionResponse resp = mapper.toResponse(bo);

        // then
        assertNotNull(resp);
        assertEquals("tan-abc12345", resp.getId());
        assertEquals(99.50, resp.getAmount(), 0.000001);
        assertNotNull(resp.getCurrency());
        assertEquals("GBP", resp.getCurrency().getValue());
        assertNotNull(resp.getType());
        assertEquals("deposit", resp.getType().getValue());
        assertEquals("Salary", resp.getReference());
        assertEquals("usr-9", resp.getUserId());
        assertEquals(now, resp.getCreatedTimestamp());
    }

    @Test
    void toResponse_returnsNullWhenInputNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toResponse_handlesNullAmount() {
        TransactionBO bo = TransactionBO.builder()
                .id("tan-x")
                .userId("usr-x")
                .amount(null)                // explicitly null
                .currency("GBP")
                .type("withdrawal")
                .created(OffsetDateTime.now())
                .build();

        TransactionResponse resp = mapper.toResponse(bo);
        assertNotNull(resp);
        assertNull(resp.getAmount(), "Amount should be null in response when BO amount is null");
        assertEquals("GBP", resp.getCurrency().getValue());
        assertEquals("withdrawal", resp.getType().getValue());
    }
}
