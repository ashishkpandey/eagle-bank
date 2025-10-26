package com.eaglebank.controller;

import com.eaglebank.bo.UserBO;
import com.eaglebank.security.AuthGuard;
import com.eaglebank.security.JwtAuthFilter;
import com.eaglebank.security.JwtUtil;
import com.eaglebank.service.user.UserService;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserApiImpl.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class UserApiImplWebMvcTest {

    private static final String BASE = "/v1/users";
    private static final String USER_ID = MockData.validUserId(); // e.g. "usr-ABC123"

    @Autowired MockMvc mvc;

    @MockBean UserService userService;
    @MockBean AuthGuard auth;

    // security beans mocked so filters never construct
    @MockBean JwtAuthFilter jwtAuthFilter;
    @MockBean JwtUtil jwtUtil;
    @MockBean UserDetailsService userDetailsService;
    @Test
    void createUser_returns201() throws Exception {
        // full, schema-valid JSON (name, email, password, phoneNumber, address)
        String json = MockData.createUserJson();

        UserBO created = MockData.userBO(USER_ID, "Test User", "test@example.com", null);
        when(userService.createUser(any(UserBO.class))).thenReturn(created);

        mvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                // optional extra assertions if you want:
                .andExpect(jsonPath("$.phoneNumber").value("+447700900123"))
                .andExpect(jsonPath("$.address.line1").value("221B Baker Street"));

        verify(userService).createUser(any(UserBO.class));
    }

    @Test
    void fetchUser_returns200() throws Exception {
        when(auth.requireUserId()).thenReturn(USER_ID);

        UserBO bo = MockData.userBO(USER_ID, "Ashish Pandey", "ashish@example.com", null);
        when(userService.getUserForRequester(USER_ID, USER_ID)).thenReturn(bo);

        mvc.perform(get(BASE + "/" + USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value("Ashish Pandey"))
                .andExpect(jsonPath("$.email").value("ashish@example.com"));
    }

    @Test
    void deleteUser_returns204() throws Exception {
        when(auth.requireUserId()).thenReturn(USER_ID);
        doNothing().when(userService).deleteUser(USER_ID, USER_ID);

        mvc.perform(delete(BASE + "/" + USER_ID))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(USER_ID, USER_ID);
    }
}
