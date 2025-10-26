package com.eaglebank.mapper.transaction;

import com.eaglebank.bo.TransactionBO;
import com.eaglebank.domain.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionEntityMapper {

  public TransactionEntity toEntity(TransactionBO bo) {
    return TransactionEntity.builder()
            .id(bo.getId())
            .accountNumber(bo.getAccountNumber())
            .userId(bo.getUserId())
            .amount(bo.getAmount())
            .currency(bo.getCurrency())
            .type(bo.getType())
            .reference(bo.getReference())
            .created(bo.getCreated())
            .build();
  }

  public TransactionBO toBO(TransactionEntity entity) {
    return TransactionBO.builder()
            .id(entity.getId())
            .accountNumber(entity.getAccountNumber())
            .userId(entity.getUserId())
            .amount(entity.getAmount())
            .currency(entity.getCurrency())
            .type(entity.getType())
            .reference(entity.getReference())
            .created(entity.getCreated())
            .build();
  }
}
