package com.eaglebank.it;

import com.eaglebank.Application;
import com.eaglebank.security.AuthGuard;
import com.eaglebank.security.JwtAuthFilter;
import com.eaglebank.security.JwtUtil;
import com.eaglebank.testutil.MockData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack integration test using real services/mappers and in-memory DAOs.
 * Security edges (AuthGuard/JWT) are mocked for determinism.
 */
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc(addFilters = false)
class EagleBankIT {

    @Autowired MockMvc mvc;

    @MockBean AuthenticationManager authenticationManager;
    @MockBean JwtUtil jwtUtil;
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean AuthGuard authGuard;

    private static final String TOKEN   = "jwt-test-token";
    private static final String USER_ID = MockData.validUserId();        // usr-2646b594
    private static final String ACC_NUM = MockData.validAccountNumber(); // 01234567
    private final ObjectMapper om = new ObjectMapper();
    private String authHeader() {
        return "Bearer " + TOKEN;
    }

    @BeforeEach
    void setupSecurity() {
        when(jwtUtil.generateToken("test@example.com")).thenReturn(TOKEN);
        when(authGuard.requireUserId()).thenReturn(USER_ID);
    }

    @Test
      void user_auth_account_transactions_end_to_end() throws Exception {
        // 1) Create User (open endpoint)
        MvcResult createUser = mvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MockData.createUserJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+447700900123"))
                .andExpect(jsonPath("$.address.line1").value("221B Baker Street"))
                // assert pattern rather than hard-coded id
                .andExpect(jsonPath("$.id", Matchers.matchesPattern("^usr-[A-Za-z0-9]{8}$")))
                .andReturn();

        String userId = om.readTree(createUser.getResponse().getContentAsByteArray())
                .get("id").asText();

// make subsequent protected calls act as this same user
        when(authGuard.requireUserId()).thenReturn(userId);

        // 2) Login -> get JWT (we stub jwtUtil to return TOKEN)
        mvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email":"test@example.com",
                  "password":"secret123"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TOKEN));

        // 3) Fetch own user (use captured userId)
        mvc.perform(get("/v1/users/{userId}", userId)
                        .header("Authorization", authHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        // 4) Create Account
        MvcResult createAcc = mvc.perform(post("/v1/accounts")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "name":"My Account",
                  "accountType":"personal"
                }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber", Matchers.matchesPattern("^01\\d{6}$")))
                .andReturn();

        String accountNumber = om.readTree(createAcc.getResponse().getContentAsByteArray())
                .get("accountNumber").asText();

        // 5) List Accounts
        mvc.perform(get("/v1/accounts")
                        .header("Authorization", authHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts[0].accountNumber").value(accountNumber));

        // 6) Fetch Account
        mvc.perform(get("/v1/accounts/{accountNumber}", accountNumber)
                        .header("Authorization", authHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(accountNumber));

        // 7) Deposit 50 GBP
        mvc.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "amount":50.0,
                  "currency":"GBP",
                  "type":"deposit",
                  "reference":"initial-deposit"
                }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("deposit"))
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andExpect(jsonPath("$.amount").value(50.0));

        // 8) Withdraw 10 GBP (succeeds)
        mvc.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "amount":10.0,
                  "currency":"GBP",
                  "type":"withdrawal",
                  "reference":"cash"
                }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("withdrawal"))
                .andExpect(jsonPath("$.amount").value(10.0));

        // 9) Withdraw 1000 GBP (insufficient funds -> 422)
        mvc.perform(post("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "amount":1000.0,
                  "currency":"GBP",
                  "type":"withdrawal",
                  "reference":"too-much"
                }
                """))
                .andExpect(status().isUnprocessableEntity());

        // 10) List Transactions
        mvc.perform(get("/v1/accounts/{accountNumber}/transactions", accountNumber)
                        .header("Authorization", authHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()", Matchers.greaterThanOrEqualTo(2)));

        // 11) Forbidden on cross-user access
        when(authGuard.requireUserId()).thenReturn("usr-OTHER999");
        mvc.perform(get("/v1/accounts/{accountNumber}", accountNumber)
                        .header("Authorization", authHeader()))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_user_missing_fields_returns_400() throws Exception {
        mvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "name":"No Address User",
                      "email":"noaddr@example.com",
                      "password":"secret123"
                    }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void create_account_missing_fields_returns_400() throws Exception {
        when(authGuard.requireUserId()).thenReturn(USER_ID);

        mvc.perform(post("/v1/accounts")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "name":""
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transaction_missing_fields_returns_400() throws Exception {
        when(authGuard.requireUserId()).thenReturn(USER_ID);

        mvc.perform(post("/v1/accounts/{accountNumber}/transactions", ACC_NUM)
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "type":"deposit"
                    }
                    """))
                .andExpect(status().isBadRequest());
    }
}
