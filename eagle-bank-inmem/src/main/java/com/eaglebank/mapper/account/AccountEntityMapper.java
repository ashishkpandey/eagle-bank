package com.eaglebank.mapper.account;

import com.eaglebank.bo.AccountBO;
import com.eaglebank.domain.AccountEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountEntityMapper {

    public AccountEntity toEntity(AccountBO bo) {
        return AccountEntity.builder()
                .accountNumber(bo.getAccountNumber())
                .userId(bo.getOwnerUserId())
                .sortCode(bo.getSortCode())
                .name(bo.getName())
                .accountType(bo.getAccountType())
                .balance(bo.getBalance())
                .currency(bo.getCurrency())
                .created(bo.getCreated())
                .updated(bo.getUpdated())
                .build();
    }

    public AccountBO toBO(AccountEntity e) {
        return AccountBO.builder()
                .accountNumber(e.getAccountNumber())
                .ownerUserId(e.getUserId())
                .sortCode(e.getSortCode())
                .name(e.getName())
                .accountType(e.getAccountType())
                .balance(e.getBalance())
                .currency(e.getCurrency())
                .created(e.getCreated())
                .updated(e.getUpdated())
                .build();
    }
}
