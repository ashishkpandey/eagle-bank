package com.eaglebank.service.user;

import com.eaglebank.bo.UserBO;
import com.eaglebank.dao.user.UserDao;
import com.eaglebank.domain.UserEntity;
import com.eaglebank.exception.ForbiddenException;
import com.eaglebank.exception.NotFoundException;
import com.eaglebank.mapper.user.UserEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserDao userDao;
  private final PasswordEncoder passwordEncoder;
  private final UserEntityMapper userEntityMapper;

  // CREATE
  public UserBO createUser(UserBO input) {
    var passwordHash = passwordEncoder.encode(
            Optional.ofNullable(input.getPasswordPlain()).orElseThrow()
    );

    var entity = userEntityMapper.toEntity(input);
    entity.setPasswordHash(passwordHash);
    userDao.save(entity);
    return userEntityMapper.toBO(entity);
  }

  // READ (with simple self-ownership check)
  public UserBO getUserForRequester(String requesterUserId, String userId) {
    var e = userDao.findById(userId).orElseThrow(NotFoundException::new);
    assertSelf(requesterUserId, e.getId());
    return userEntityMapper.toBO(e);
  }

  // DELETE
  public void deleteUser(String requesterUserId, String userId) {
    var e = userDao.findById(userId).orElseThrow(NotFoundException::new);
    assertSelf(requesterUserId, e.getId());
    // Add “cannot delete if has accounts” logic where appropriate
    // (your AccountDao can be injected to check)
    // userDao.delete(userId); // add to DAO if needed
  }

  // For authentication
  public Optional<UserEntity> findByEmail(String email) {
    return userDao.findByEmail(email);
  }

  private void assertSelf(String requesterUserId, String ownerUserId) {
    if (!requesterUserId.equals(ownerUserId)) throw new ForbiddenException();
  }


}
