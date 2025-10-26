package com.eaglebank.controller;

import com.eaglebank.bo.TransactionBO;
import com.eaglebank.gen.model.CreateTransactionRequest;
import com.eaglebank.gen.model.TransactionResponse;
import com.eaglebank.mapper.transaction.TransactionApiMapper;
import com.eaglebank.security.AuthGuard;
import com.eaglebank.security.JwtAuthFilter;
import com.eaglebank.security.JwtUtil;
import com.eaglebank.service.transaction.TransactionService;
import com.eaglebank.testutil.MockData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TransactionApiImpl.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class TransactionApiImplWebMvcTest {

    private static final String BASE = "/v1/accounts";
    private static final String ACC_NUM = MockData.validAccountNumber(); // "01234567"
    private static final String TX_ID  = MockData.validTransactionId();  // "tan-ABC12345"

    @Autowired MockMvc mvc;

    @MockBean TransactionService transactionService;
    @MockBean AuthGuard authGuard;
    @MockBean TransactionApiMapper mapper;

    // mock security bits
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean JwtUtil jwtUtil;
    @MockBean UserDetailsService userDetailsService;

    @Test
    void createTransaction_returns201() throws Exception {
        when(authGuard.requireUserId()).thenReturn("u1");

        // Request/BO/Response via MockData; enums: type "deposit", currency "GBP"
        CreateTransactionRequest req = MockData.txRequest(50.0, "deposit", "GBP", "test-dep");
        TransactionBO in  = MockData.txBO("u1", ACC_NUM, "deposit", new BigDecimal("50.0"), "GBP");
        TransactionBO out = MockData.txBO("u1", ACC_NUM, "deposit", new BigDecimal("50.0"), "GBP");
        out.setId(TX_ID);

        TransactionResponse resp = new TransactionResponse()
                .id(TX_ID)
                .amount(50.0)
                .type(TransactionResponse.TypeEnum.DEPOSIT)
                .currency(TransactionResponse.CurrencyEnum.GBP)
                .reference("test-dep");

        when(mapper.fromRequest(any(CreateTransactionRequest.class), eq(ACC_NUM), eq("u1"))).thenReturn(in);
        when(transactionService.createTransaction(in)).thenReturn(out);
        when(mapper.toResponse(out)).thenReturn(resp);

        mvc.perform(post(BASE + "/" + ACC_NUM + "/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"amount":50.0,"currency":"GBP","type":"deposit","reference":"test-dep"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(TX_ID))
                .andExpect(jsonPath("$.amount").value(50.0))
                .andExpect(jsonPath("$.type").value("deposit"))
                .andExpect(jsonPath("$.currency").value("GBP"));

        verify(transactionService).createTransaction(in);
    }

    @Test
    void listTransactions_returnsOk() throws Exception {
        when(authGuard.requireUserId()).thenReturn("u1");

        TransactionBO bo1 = MockData.txBO("u1", ACC_NUM, "deposit", new BigDecimal("10"), "GBP");
        bo1.setId("tan-AAA111");
        TransactionBO bo2 = MockData.txBO("u1", ACC_NUM, "withdrawal", new BigDecimal("5"), "GBP");
        bo2.setId("tan-BBB222");

        TransactionResponse r1 = new TransactionResponse()
                .id("tan-AAA111")
                .amount(10.0)
                .type(TransactionResponse.TypeEnum.DEPOSIT)
                .currency(TransactionResponse.CurrencyEnum.GBP);
        TransactionResponse r2 = new TransactionResponse()
                .id("tan-BBB222")
                .amount(5.0)
                .type(TransactionResponse.TypeEnum.WITHDRAWAL)
                .currency(TransactionResponse.CurrencyEnum.GBP);

        when(transactionService.listTransactions("u1", ACC_NUM)).thenReturn(List.of(bo1, bo2));
        when(mapper.toResponse(bo1)).thenReturn(r1);
        when(mapper.toResponse(bo2)).thenReturn(r2);

        mvc.perform(get(BASE + "/" + ACC_NUM + "/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions[0].id").value("tan-AAA111"))
                .andExpect(jsonPath("$.transactions[1].id").value("tan-BBB222"));
    }

    @Test
    void fetchTransaction_returnsOk() throws Exception {
        when(authGuard.requireUserId()).thenReturn("u1");

        TransactionBO bo = MockData.txBO("u1", ACC_NUM, "deposit", new BigDecimal("25"), "GBP");
        bo.setId(TX_ID);
        TransactionResponse resp = new TransactionResponse()
                .id(TX_ID)
                .amount(25.0)
                .type(TransactionResponse.TypeEnum.DEPOSIT)
                .currency(TransactionResponse.CurrencyEnum.GBP);

        when(transactionService.getTransactionForUser("u1", ACC_NUM, TX_ID)).thenReturn(bo);
        when(mapper.toResponse(bo)).thenReturn(resp);

        mvc.perform(get(BASE + "/" + ACC_NUM + "/transactions/" + TX_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TX_ID))
                .andExpect(jsonPath("$.amount").value(25.0))
                .andExpect(jsonPath("$.type").value("deposit"))
                .andExpect(jsonPath("$.currency").value("GBP"));
    }
}
