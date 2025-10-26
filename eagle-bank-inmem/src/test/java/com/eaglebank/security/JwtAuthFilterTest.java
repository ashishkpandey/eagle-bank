package com.eaglebank.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtUtil jwtUtil;
    private UserDetailsService uds;
    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        uds = mock(UserDetailsService.class);
        filter = new JwtAuthFilter(jwtUtil, uds);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ---------- shouldNotFilter (public endpoints & OPTIONS) ----------

    @Test
    void shouldNotFilter_authLogin() {
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.POST.name(), "/v1/auth/login");
        req.setServletPath("/v1/auth/login"); // IMPORTANT for AntPathRequestMatcher
        assertTrue(filter.shouldNotFilter(req));
    }

    @Test
    void shouldNotFilter_createUser() {
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.POST.name(), "/v1/users");
        req.setServletPath("/v1/users");
        assertTrue(filter.shouldNotFilter(req));
    }

    @Test
    void shouldNotFilter_swaggerUi() {
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.GET.name(), "/swagger-ui/index.html");
        req.setServletPath("/swagger-ui/index.html");
        assertTrue(filter.shouldNotFilter(req));
    }

    @Test
    void shouldNotFilter_openApiDocs() {
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.GET.name(), "/v3/api-docs/some");
        req.setServletPath("/v3/api-docs/some");
        assertTrue(filter.shouldNotFilter(req));
    }

    @Test
    void shouldNotFilter_actuator() {
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.GET.name(), "/actuator/health");
        req.setServletPath("/actuator/health");
        assertTrue(filter.shouldNotFilter(req));
    }

    @Test
    void shouldNotFilter_corsPreflight() {
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.OPTIONS.name(), "/anything");
        req.setServletPath("/anything");
        assertTrue(filter.shouldNotFilter(req));
    }

    // ---------- doFilterInternal (protected endpoints) ----------

    @Test
    void doFilter_setsAuthentication_onValidBearerToken() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.GET.name(), "/v1/accounts/a1");
        req.setServletPath("/v1/accounts/a1");
        req.addHeader("Authorization", "Bearer good.token.here");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        when(jwtUtil.validateAndGetSubject("good.token.here")).thenReturn("user@example.com");
        UserDetails ud = new User("user@example.com", "{noop}pw",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(uds.loadUserByUsername("user@example.com")).thenReturn(ud);

        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(req, resp, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Authentication should be set");
        assertEquals("user@example.com", auth.getName());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        verify(chain, times(1)).doFilter(req, resp);
    }

    @Test
    void doFilter_doesNothing_whenNoAuthorizationHeader() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.GET.name(), "/v1/accounts/a1");
        req.setServletPath("/v1/accounts/a1");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(req, resp, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Auth should not be set");
        verify(chain, times(1)).doFilter(req, resp);
        verifyNoInteractions(jwtUtil, uds);
    }

    @Test
    void doFilter_ignoresBearerNull() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.GET.name(), "/v1/accounts/a1");
        req.setServletPath("/v1/accounts/a1");
        req.addHeader("Authorization", "Bearer null");
        MockHttpServletResponse resp = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(req, resp, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Auth should not be set for 'Bearer null'");
        verify(chain, times(1)).doFilter(req, resp);
        verifyNoInteractions(jwtUtil, uds);
    }

    @Test
    void doFilter_invalidToken_leavesContextNull() throws ServletException, IOException {
        // Do NOT pre-set auth; filter only processes JWT if auth is currently null
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.GET.name(), "/v1/accounts/a1");
        req.setServletPath("/v1/accounts/a1");
        req.addHeader("Authorization", "Bearer bad.token");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        when(jwtUtil.validateAndGetSubject("bad.token")).thenThrow(new RuntimeException("expired/invalid"));

        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(req, resp, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication(),
                "Auth should remain null when token is invalid");
        verify(chain, times(1)).doFilter(req, resp);
    }

    @Test
    void doFilter_skipsReauth_ifAlreadyAuthenticated() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest(HttpMethod.GET.name(), "/v1/accounts/a1");
        req.setServletPath("/v1/accounts/a1");
        req.addHeader("Authorization", "Bearer whatever");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        // Pre-set authentication, the filter should not overwrite it
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("preauth", null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(req, resp, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("preauth", auth.getName(), "Existing auth should remain");
        verifyNoInteractions(jwtUtil, uds);
        verify(chain, times(1)).doFilter(req, resp);
    }
}
