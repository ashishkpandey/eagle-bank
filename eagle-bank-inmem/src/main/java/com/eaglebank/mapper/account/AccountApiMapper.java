package com.eaglebank.mapper.account;

import com.eaglebank.bo.AccountBO;
import com.eaglebank.gen.model.BankAccountResponse;
import com.eaglebank.gen.model.CreateBankAccountRequest;
import com.eaglebank.util.IdGenerator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Component
public class AccountApiMapper {

    /** Build a BO from the create request (controller boundary). */
    public AccountBO fromCreateRequest(CreateBankAccountRequest req, String userId) {
        return AccountBO.builder()
                .accountNumber(IdGenerator.accountId())
                .ownerUserId(userId)
                .sortCode("10-10-10")
                .name(req.getName())
                .accountType(req.getAccountType() != null ? req.getAccountType().getValue() : "personal")
                .balance(BigDecimal.ZERO)
                .currency("GBP")
                .created(OffsetDateTime.now())
                .updated(OffsetDateTime.now())
                .build();
    }

    /** Map BO to OpenAPI response model. */
    public BankAccountResponse toResponse(AccountBO bo) {
        return new BankAccountResponse()
                .accountNumber(bo.getAccountNumber())
                .sortCode(BankAccountResponse.SortCodeEnum.fromValue(bo.getSortCode()))
                .name(bo.getName())
                .accountType(BankAccountResponse.AccountTypeEnum.fromValue(bo.getAccountType()))
                .balance(bo.getBalance().doubleValue())
                .currency(BankAccountResponse.CurrencyEnum.fromValue(bo.getCurrency()))
                .createdTimestamp(bo.getCreated())
                .updatedTimestamp(bo.getUpdated());
    }


}
