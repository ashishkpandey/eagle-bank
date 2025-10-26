package com.eaglebank.controller;

import com.eaglebank.bo.AccountBO;
import com.eaglebank.gen.model.BankAccountResponse;
import com.eaglebank.gen.model.CreateBankAccountRequest;
import com.eaglebank.mapper.account.AccountApiMapper;
import com.eaglebank.security.AuthGuard;
import com.eaglebank.security.JwtAuthFilter;
import com.eaglebank.security.JwtUtil;
import com.eaglebank.service.account.AccountService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AccountApiImpl.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class AccountApiImplWebMvcTest {

    private static final String BASE = "/v1/accounts";
    private static final String ACC_NUM = "01234567";

    @Autowired MockMvc mvc;

    @MockBean AccountService accountService;
    @MockBean AuthGuard authGuard;
    @MockBean AccountApiMapper mapper;

    // mock security bits so nothing tries to build real beans
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean JwtUtil jwtUtil;
    @MockBean UserDetailsService userDetailsService;

    @Test
    void createAccount_returns201() throws Exception {
        when(authGuard.requireUserId()).thenReturn("u1");

        // Request and mapped objects
        CreateBankAccountRequest req = new CreateBankAccountRequest();
        req.setName("My Account");
        req.accountType(CreateBankAccountRequest.AccountTypeEnum.PERSONAL);

        AccountBO bo = MockData.accountBO("u1", ACC_NUM, new BigDecimal("100"), "GBP");

        BankAccountResponse resp = new BankAccountResponse()
                .accountNumber(ACC_NUM)
                .balance(100.0)
                .currency(BankAccountResponse.CurrencyEnum.GBP);

        when(mapper.fromCreateRequest(any(CreateBankAccountRequest.class), eq("u1"))).thenReturn(bo);
        when(accountService.createAccount(bo)).thenReturn(bo);
        when(mapper.toResponse(bo)).thenReturn(resp);

        mvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"name":"My Account","accountType":"personal"}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value(ACC_NUM))
                .andExpect(jsonPath("$.balance").value(100.0))
                .andExpect(jsonPath("$.currency").value("GBP"));

        verify(accountService).createAccount(bo);
    }

    @Test
    void listAccounts_returnsOkWithList() throws Exception {
        when(authGuard.requireUserId()).thenReturn("u1");

        AccountBO a1 = MockData.accountBO("u1", ACC_NUM, new BigDecimal("100"), "GBP");
        AccountBO a2 = MockData.accountBO("u1", "01234568", new BigDecimal("200"), "GBP");
        when(accountService.listUserAccounts("u1")).thenReturn(List.of(a1, a2));

        BankAccountResponse r1 = new BankAccountResponse()
                .accountNumber(ACC_NUM).balance(100.0).currency(BankAccountResponse.CurrencyEnum.GBP);
        BankAccountResponse r2 = new BankAccountResponse()
                .accountNumber("01234568").balance(200.0).currency(BankAccountResponse.CurrencyEnum.GBP);
        when(mapper.toResponse(a1)).thenReturn(r1);
        when(mapper.toResponse(a2)).thenReturn(r2);

        mvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts[0].accountNumber").value(ACC_NUM))
                .andExpect(jsonPath("$.accounts[1].accountNumber").value("01234568"));
    }

    @Test
    void fetchAccount_returnsAccount() throws Exception {
        when(authGuard.requireUserId()).thenReturn("u1");

        AccountBO bo = MockData.accountBO("u1", ACC_NUM, new BigDecimal("123"), "GBP");
        BankAccountResponse resp = new BankAccountResponse()
                .accountNumber(ACC_NUM)
                .balance(123.0)
                .currency(BankAccountResponse.CurrencyEnum.GBP);

        when(accountService.getAccountForUser("u1", ACC_NUM)).thenReturn(bo);
        when(mapper.toResponse(bo)).thenReturn(resp);

        mvc.perform(get(BASE + "/" + ACC_NUM))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(ACC_NUM))
                .andExpect(jsonPath("$.balance").value(123.0));
    }

    @Test
    void updateAccountByNumber_returnsUpdated() throws Exception {
        when(authGuard.requireUserId()).thenReturn("u1");

        AccountBO bo = MockData.accountBO("u1", ACC_NUM, new BigDecimal("500"), "GBP");
        bo.setName("Updated Name");

        BankAccountResponse resp = new BankAccountResponse()
                .accountNumber(ACC_NUM)
                .name("Updated Name");

        when(accountService.updateAccountName("u1", ACC_NUM, "Updated Name")).thenReturn(bo);
        when(mapper.toResponse(bo)).thenReturn(resp);

        mvc.perform(patch(BASE + "/" + ACC_NUM)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void deleteAccountByNumber_returns204() throws Exception {
        when(authGuard.requireUserId()).thenReturn("u1");
        doNothing().when(accountService).deleteAccount("u1", ACC_NUM);

        mvc.perform(delete(BASE + "/" + ACC_NUM))
                .andExpect(status().isNoContent());

        verify(accountService).deleteAccount("u1", ACC_NUM);
    }
}
