package com.eaglebank.mapper.transaction;

import com.eaglebank.bo.TransactionBO;
import com.eaglebank.gen.model.CreateTransactionRequest;
import com.eaglebank.gen.model.TransactionResponse;
import com.eaglebank.util.IdGenerator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Component
public class TransactionApiMapper {
  public TransactionBO fromRequest(CreateTransactionRequest req, String accountNumber, String userId) {
    return TransactionBO.builder()
            .id(IdGenerator.transactionId())
            .accountNumber(accountNumber)
            .userId(userId)
            .amount(req.getAmount() == null ? BigDecimal.ZERO : BigDecimal.valueOf(req.getAmount()))
            .currency(req.getCurrency() != null ? req.getCurrency().getValue() : "GBP")
            .type(req.getType() != null ? req.getType().getValue() : "deposit")
            .reference(req.getReference())
            .created(OffsetDateTime.now())
            .build();
  }
  public TransactionResponse toResponse(TransactionBO bo) {
    if (bo == null) return null;

    return new TransactionResponse()
            .id(bo.getId())
            .amount(bo.getAmount() != null ? bo.getAmount().doubleValue() : null)
            .currency(
                    bo.getCurrency() != null
                            ? TransactionResponse.CurrencyEnum.fromValue(bo.getCurrency())
                            : null
            )
            .type(
                    bo.getType() != null
                            ? TransactionResponse.TypeEnum.fromValue(bo.getType())
                            : null
            )
            .reference(bo.getReference())
            .userId(bo.getUserId())
            .createdTimestamp(bo.getCreated());
  }
}
