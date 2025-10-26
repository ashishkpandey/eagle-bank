package com.eaglebank.testutil;

import com.eaglebank.bo.AccountBO;
import com.eaglebank.bo.TransactionBO;
import com.eaglebank.bo.UserBO;
import com.eaglebank.domain.AccountEntity;
import com.eaglebank.domain.TransactionEntity;
import com.eaglebank.domain.UserEntity;
import com.eaglebank.gen.model.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Centralised factory for reusable mock/test objects.
 * Keeps tests clean and consistent.
 */
public final class MockData {

    private MockData() {}

    /* ---------- Valid identifiers ---------- */

    public static String validUserId() { return "usr-2646b594"; }
    public static String validAccountNumber() { return "01234567"; }
    public static String validTransactionId() { return "tan-XYZ98765"; }
    public static String validPhone() { return "+447700900123"; }

    /* ---------- Domain BO creators ---------- */
    public static UserEntity userEntity(String id, String email, String name) {
        UserEntity u = new UserEntity();
        u.setId(id);
        u.setEmail(email);
        u.setName(name);
        return u;
    }
    public static UserBO userBO(String id, String name, String email, String passwordPlain) {
        UserBO.AddressBO addr = UserBO.AddressBO.builder()
                .line1("221B Baker Street")
                .line2("Flat B")
                .line3("")
                .town("London")
                .county("Greater London")
                .postcode("NW1 6XE")
                .build();

        return UserBO.builder()
                .id(id)
                .name(name)
                .email(email)
                .passwordPlain(passwordPlain)
                .phoneNumber("+447700900123")
                .address(addr)
                .created(OffsetDateTime.now())
                .updated(OffsetDateTime.now())
                .build();
    }

    public static AccountEntity accountEntity(String userId, String number, BigDecimal balance, String currency) {
        AccountEntity e = new AccountEntity();
        e.setUserId(userId);
        e.setAccountNumber(number);
        e.setBalance(balance);
        e.setCurrency(currency);
        e.setCreated(OffsetDateTime.now());
        e.setUpdated(OffsetDateTime.now());
        return e;
    }
    public static AccountBO accountBO(String userId, String accountNumber, BigDecimal balance, String currency) {
        AccountBO bo = new AccountBO();
        bo.setOwnerUserId(userId);
        bo.setAccountNumber(accountNumber);
        bo.setName("Personal Account");
        bo.setBalance(balance);
        bo.setCurrency(currency);
        bo.setCreated(OffsetDateTime.now());
        bo.setUpdated(OffsetDateTime.now());
        return bo;
    }
    public static TransactionEntity txEntity(String id, String userId, String accountNumber, String type, BigDecimal amount, String currency) {
        TransactionEntity t = new TransactionEntity();
        t.setId(id);
        t.setUserId(userId);
        t.setAccountNumber(accountNumber);
        t.setType(type);
        t.setAmount(amount);
        t.setCurrency(currency);
        t.setCreated(OffsetDateTime.now());
        return t;
    }
    public static TransactionBO txBO(String userId, String accountNumber, String type,
                                     BigDecimal amount, String currency) {
        TransactionBO bo = new TransactionBO();
        bo.setUserId(userId);
        bo.setAccountNumber(accountNumber);
        bo.setType(type);
        bo.setAmount(amount);
        bo.setCurrency(currency);
        bo.setReference("TestRef");
        bo.setCreated(OffsetDateTime.now());
        return bo;
    }

    /* ---------- API request creators ---------- */

    public static CreateUserRequest createUserRequest() {
        var req = new CreateUserRequest();
        req.setName("Test User");
        req.setEmail("test@example.com");
        req.setPassword("secret123");
        req.setPhoneNumber("+447700900123");

        CreateUserRequestAddress address = new CreateUserRequestAddress()
                .line1("221B Baker Street")
                .line2("Flat B")
                .line3("")
                .town("London")
                .county("Greater London")
                .postcode("NW1 6XE");

        req.setAddress(address);
        return req;
    }


    public static CreateBankAccountRequest createAccountRequest() {
        var req = new CreateBankAccountRequest();
        req.setName("Savings Account");
        req.setAccountType(CreateBankAccountRequest.AccountTypeEnum.PERSONAL);
        return req;
    }

    public static CreateTransactionRequest txRequest(double amount, String typeLower, String currencyUpper, String reference) {
        var req = new CreateTransactionRequest();
        req.setAmount(amount);
        req.setCurrency(CreateTransactionRequest.CurrencyEnum.valueOf(currencyUpper));
        req.setType("deposit".equalsIgnoreCase(typeLower)
                ? CreateTransactionRequest.TypeEnum.DEPOSIT
                : CreateTransactionRequest.TypeEnum.WITHDRAWAL);
        req.setReference(reference);
        return req;
    }

    /* ---------- Convenience JSON snippets ---------- */

    public static String addressJson() {
        return """
      {
        "line1":"221B Baker Street",
        "line2":"Flat B",
        "line3":"",
        "town":"London",
        "county":"Greater London",
        "postcode":"NW1 6XE"
      }
      """;
    }

    public static String createUserJson() {
        return """
      {
        "name":"Test User",
        "address":{
          "line1":"221B Baker Street",
          "line2":"Flat B",
          "line3":"",
          "town":"London",
          "county":"Greater London",
          "postcode":"NW1 6XE"
        },
        "phoneNumber":"+447700900123",
        "email":"test@example.com",
        "password":"secret123"
      }
      """;
    }

    public static String createAccountJson() {
        return """
      {
        "name":"My Account",
        "currency":"GBP",
        "accountType":"PERSONAL"
      }
      """;
    }

    public static String createTransactionJson() {
        return """
      {
        "amount":50.0,
        "currency":"GBP",
        "type":"deposit",
        "reference":"test-dep"
      }
      """;
    }

    /* ---------- Expected response creators ---------- */

    public static UserResponse userResponse() {

        CreateUserRequestAddress addr = new CreateUserRequestAddress()
                .line1("221B Baker Street")
                .line2("Flat B")
                .line3("")
                .town("London")
                .county("Greater London")
                .postcode("NW1 6XE");

        return new UserResponse()
                .id(validUserId())
                .name("Test User")
                .address(addr)
                .phoneNumber("+447700900123")
                .email("test@example.com")
                .createdTimestamp(OffsetDateTime.now())
                .updatedTimestamp(OffsetDateTime.now());
    }

    public static String loginJson() {
        return """
                    {
                      "email":"test@example.com",
                      "password":"secret123"
                    }
                    """;
    }
}
