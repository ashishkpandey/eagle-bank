package com.eaglebank.controller;


import com.eaglebank.gen.api.AccountApi;
import com.eaglebank.gen.model.BankAccountResponse;
import com.eaglebank.gen.model.CreateBankAccountRequest;
import com.eaglebank.gen.model.ListBankAccountsResponse;
import com.eaglebank.gen.model.UpdateBankAccountRequest;
import com.eaglebank.mapper.account.AccountApiMapper;
import com.eaglebank.security.AuthGuard;
import com.eaglebank.service.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.CREATED;


@RestController
@RequiredArgsConstructor
public class AccountApiImpl implements AccountApi {

  private final AccountService accountService;
  private final AuthGuard authGuard;
  private final AccountApiMapper accountApiMapper;
  @Override
  public ResponseEntity<BankAccountResponse> createAccount(CreateBankAccountRequest body) {
    var uid = authGuard.requireUserId(); // throws NotAuthenticatedException

    var acc = accountService.createAccount(accountApiMapper.fromCreateRequest(body,authGuard.requireUserId()));
    return ResponseEntity.status(CREATED).body(accountApiMapper.toResponse((acc)));
  }

  @Override
  public ResponseEntity<ListBankAccountsResponse> listAccounts() {

    var list = accountService.listUserAccounts(authGuard.requireUserId()).stream()
            .map(accountApiMapper::toResponse)
            .collect(Collectors.toList());
    return ResponseEntity.ok(new ListBankAccountsResponse().accounts(list));
  }

  @Override
  public ResponseEntity<BankAccountResponse> fetchAccountByAccountNumber(String accountNumber) {

    var acc = accountService.getAccountForUser(authGuard.requireUserId(), accountNumber); // includes ownership check
    return ResponseEntity.ok(accountApiMapper.toResponse(acc));
  }

  @Override
  public ResponseEntity<BankAccountResponse> updateAccountByAccountNumber(
          String accountNumber, UpdateBankAccountRequest body) {

    var acc = accountService.updateAccountName(authGuard.requireUserId(), accountNumber, body.getName());
    return ResponseEntity.ok(accountApiMapper.toResponse(acc));
  }

  @Override
  public ResponseEntity<Void> deleteAccountByAccountNumber(String accountNumber) {
    accountService.deleteAccount(authGuard.requireUserId(), accountNumber);
    return ResponseEntity.noContent().build();
  }
}
