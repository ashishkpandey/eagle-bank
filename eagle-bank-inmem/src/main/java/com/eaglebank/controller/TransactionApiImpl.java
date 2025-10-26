package com.eaglebank.controller;

import com.eaglebank.gen.api.TransactionApi;
import com.eaglebank.gen.model.CreateTransactionRequest;
import com.eaglebank.gen.model.ListTransactionsResponse;
import com.eaglebank.gen.model.TransactionResponse;
import com.eaglebank.mapper.transaction.TransactionApiMapper;
import com.eaglebank.security.AuthGuard;
import com.eaglebank.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
public class TransactionApiImpl implements TransactionApi {

  private final AuthGuard authGuard;
  private final TransactionService transactionService;
  private final TransactionApiMapper txMapper;

  @Override
  public ResponseEntity<TransactionResponse> createTransaction(String accountNumber, CreateTransactionRequest body) {
    String userId = authGuard.requireUserId();
    var bo = txMapper.fromRequest(body, accountNumber, userId);
    var tx = transactionService.createTransaction(bo);
    return ResponseEntity.status(CREATED).body(txMapper.toResponse(tx));
  }

  @Override
  public ResponseEntity<ListTransactionsResponse> listAccountTransaction(String accountNumber) {
    String userId = authGuard.requireUserId();
    var items = transactionService.listTransactions(userId, accountNumber)
            .stream()
            .map(txMapper::toResponse)
            .toList();
    return ResponseEntity.ok(new ListTransactionsResponse().transactions(items));
  }

  @Override
  public ResponseEntity<TransactionResponse> fetchAccountTransactionByID(String accountNumber, String transactionId) {
    String userId = authGuard.requireUserId();
    var tx = transactionService.getTransactionForUser(userId, accountNumber, transactionId);
    return ResponseEntity.ok(txMapper.toResponse(tx));
  }
}
