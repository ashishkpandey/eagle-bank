package com.eaglebank.controller;

import com.eaglebank.security.JwtAuthFilter;
import com.eaglebank.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerWebMvcTest {

    @Autowired MockMvc mvc;

    @MockBean AuthenticationManager authenticationManager;
    @MockBean JwtUtil jwtUtil;

    // mock security infra so context loads without real beans
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean UserDetailsService userDetailsService;

    private static final String BASE = "/v1/auth";

    @Test
    void login_success_returns200WithToken() throws Exception {
        when(jwtUtil.generateToken("test@example.com")).thenReturn("jwt123");

        mvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"email":"test@example.com","password":"secret123"}
                """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt123"));

        verify(authenticationManager).authenticate(any());
        verify(jwtUtil).generateToken("test@example.com");
    }

    @Test
    void login_failure_returns401() throws Exception {
        doThrow(new BadCredentialsException("bad creds"))
                .when(authenticationManager).authenticate(any());

        mvc.perform(post(BASE + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"email":"test@example.com","password":"wrong"}
                """))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager).authenticate(any());
        verify(jwtUtil, never()).generateToken(any());
    }
}
