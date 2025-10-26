package com.eaglebank.controller;

import com.eaglebank.gen.api.UserApi;
import com.eaglebank.gen.model.CreateUserRequest;
import com.eaglebank.gen.model.UpdateUserRequest;
import com.eaglebank.gen.model.UserResponse;
import com.eaglebank.mapper.user.UserApiMapper;
import com.eaglebank.security.AuthGuard;
import com.eaglebank.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
public class UserApiImpl implements UserApi {

  private final UserService userService;
  private final AuthGuard auth;

  @Override
  public ResponseEntity<UserResponse> createUser(CreateUserRequest request) {
    if (request.getEmail() == null || request.getName() == null) {
      throw new IllegalArgumentException("name and email must not be null");
    }
    var bo = UserApiMapper.fromCreateRequest(request);
    var created = userService.createUser(bo);
    return ResponseEntity.status(CREATED).body(UserApiMapper.toResponse(created));
  }

  @Override
  public ResponseEntity<UserResponse> fetchUserByID(String userId) {
    var current = auth.requireUserId();
    var bo = userService.getUserForRequester(current, userId);
    return ResponseEntity.ok(UserApiMapper.toResponse(bo));
  }

  @Override
  public ResponseEntity<UserResponse> updateUserByID(String userId, UpdateUserRequest body) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  @Override
  public ResponseEntity<Void> deleteUserByID(String userId) {
    var current = auth.requireUserId();
    userService.deleteUser(current, userId);
    return ResponseEntity.noContent().build();
  }
}
