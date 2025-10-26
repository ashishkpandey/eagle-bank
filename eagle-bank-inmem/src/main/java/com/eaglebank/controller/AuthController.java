package com.eaglebank.controller;

import com.eaglebank.gen.model.LoginRequest;
import com.eaglebank.gen.model.TokenResponse;
import com.eaglebank.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/auth")
public class AuthController {

  private final AuthenticationManager authManager;
  private final JwtUtil jwt;

  public AuthController(AuthenticationManager authManager, JwtUtil jwt) {
    this.authManager = authManager;
    this.jwt = jwt;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest req) {
    try {
      authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
      return ResponseEntity.ok(new TokenResponse(jwt.generateToken(req.getEmail())));
    } catch (AuthenticationException ex) {
      return ResponseEntity.status(401).build();
    }
  }
}
