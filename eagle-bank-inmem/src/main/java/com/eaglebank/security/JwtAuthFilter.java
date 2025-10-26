package com.eaglebank.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserDetailsService uds;

  // ✅ match public routes & preflight with RequestMatchers (robust)
  private final RequestMatcher publicEndpoints = new OrRequestMatcher(
          new AntPathRequestMatcher("/v1/users", HttpMethod.POST.name()),
          new AntPathRequestMatcher("/v1/auth/login", HttpMethod.POST.name()),
          new AntPathRequestMatcher("/v3/api-docs/**"),
          new AntPathRequestMatcher("/swagger-ui/**"),
          new AntPathRequestMatcher("/actuator/**"),
          new AntPathRequestMatcher("/**", HttpMethod.OPTIONS.name()) // CORS preflight
  );

  public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsService uds) {
    this.jwtUtil = jwtUtil;
    this.uds = uds;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return publicEndpoints.matches(request);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
          throws ServletException, IOException {

    String header = request.getHeader("Authorization");
    boolean hasBearer = StringUtils.hasText(header) && header.startsWith("Bearer ");
    boolean tokenLooksNull = hasBearer && header.regionMatches(true, 7, "null", 0, 4);

    if (hasBearer && !tokenLooksNull && SecurityContextHolder.getContext().getAuthentication() == null) {
      try {
        String email = jwtUtil.validateAndGetSubject(header.substring(7)); // throws if invalid/expired
        if (email != null) {
          UserDetails user = uds.loadUserByUsername(email);
          var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
          auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      } catch (Exception e) {
        // invalid/expired token → clear context and continue (framework will 401 if required)
        e.printStackTrace();
        SecurityContextHolder.clearContext();
      }
    }

    chain.doFilter(request, response);
  }
}
