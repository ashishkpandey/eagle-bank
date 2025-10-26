package com.eaglebank.service.transaction;

import com.eaglebank.bo.TransactionBO;
import com.eaglebank.dao.account.AccountDao;
import com.eaglebank.dao.transaction.TransactionDao;
import com.eaglebank.domain.AccountEntity;
import com.eaglebank.domain.TransactionEntity;
import com.eaglebank.exception.ForbiddenException;
import com.eaglebank.exception.InsufficientFundsException;
import com.eaglebank.exception.NotFoundException;
import com.eaglebank.mapper.transaction.TransactionEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    private TransactionDao txDao;
    private AccountDao accountDao;
    private TransactionService service;

    @BeforeEach
    void setUp() {
        txDao = mock(TransactionDao.class);
        accountDao = mock(AccountDao.class);
        service = new TransactionService(txDao, accountDao, new TransactionEntityMapper());
    }

    /* --------------------- createTransaction --------------------- */

    @Test
    void deposit_updatesBalance_andPersists() {
        AccountEntity acc = account("acc-1", "usr-1", "GBP", "100");
        when(accountDao.findByNumber("acc-1")).thenReturn(Optional.of(acc));

        TransactionBO input = txBO("usr-1", "acc-1", "deposit", "50", "GBP", "ref-1");
        TransactionBO saved = service.createTransaction(input);

        assertEquals(new BigDecimal("150"), acc.getBalance(), "balance must increase");
        verify(accountDao, times(1)).save(acc);
        verify(txDao, times(1)).save(any(TransactionEntity.class));

        assertNotNull(saved.getId());
        assertTrue(saved.getId().startsWith("tan-"));
        assertEquals("deposit", saved.getType());
        assertEquals(new BigDecimal("50"), saved.getAmount());
        assertEquals("usr-1", saved.getUserId());
        assertEquals("acc-1", saved.getAccountNumber());
        assertEquals("GBP", saved.getCurrency());
        assertEquals("ref-1", saved.getReference());
        assertNotNull(saved.getCreated());
    }

    @Test
    void withdrawal_updatesBalance_andPersists() {
        AccountEntity acc = account("acc-1", "usr-1", "GBP", "100");
        when(accountDao.findByNumber("acc-1")).thenReturn(Optional.of(acc));

        TransactionBO input = txBO("usr-1", "acc-1", "withdrawal", "40", "GBP", null);
        TransactionBO saved = service.createTransaction(input);

        assertEquals(new BigDecimal("60"), acc.getBalance(), "balance must decrease");
        verify(accountDao).save(acc);
        verify(txDao).save(any(TransactionEntity.class));

        assertEquals("withdrawal", saved.getType());
        assertEquals(new BigDecimal("40"), saved.getAmount());
    }

    @Test
    void withdrawal_insufficientFunds_throws422_andDoesNotPersist() {
        AccountEntity acc = account("acc-1", "usr-1", "GBP", "30");
        when(accountDao.findByNumber("acc-1")).thenReturn(Optional.of(acc));

        TransactionBO input = txBO("usr-1", "acc-1", "withdrawal", "60", "GBP", null);

        assertThrows(InsufficientFundsException.class, () -> service.createTransaction(input));
        verify(txDao, never()).save(any());
        verify(accountDao, never()).save(any());
    }

    @Test
    void create_forOtherUsersAccount_throws403_andDoesNotPersist() {
        AccountEntity acc = account("acc-1", "owner-1", "GBP", "100");
        when(accountDao.findByNumber("acc-1")).thenReturn(Optional.of(acc));

        TransactionBO input = txBO("usr-2", "acc-1", "deposit", "10", "GBP", null);
        assertThrows(ForbiddenException.class, () -> service.createTransaction(input));
        verify(txDao, never()).save(any());
        verify(accountDao, never()).save(any());
    }

    @Test
    void create_onMissingAccount_throws404() {
        when(accountDao.findByNumber("missing")).thenReturn(Optional.empty());
        TransactionBO input = txBO("usr-1", "missing", "deposit", "10", "GBP", null);
        assertThrows(NotFoundException.class, () -> service.createTransaction(input));
    }

    @Test
    void create_invalidAmountNull_throws400() {
        AccountEntity acc = account("acc-1", "usr-1", "GBP", "100");
        when(accountDao.findByNumber("acc-1")).thenReturn(Optional.of(acc));

        TransactionBO input = txBO("usr-1", "acc-1", "deposit", null, "GBP", null);
        assertThrows(IllegalArgumentException.class, () -> service.createTransaction(input));
    }

    @Test
    void create_invalidAmountNegative_throws400() {
        AccountEntity acc = account("acc-1", "usr-1", "GBP", "100");
        when(accountDao.findByNumber("acc-1")).thenReturn(Optional.of(acc));

        TransactionBO input = txBO("usr-1", "acc-1", "deposit", "-1", "GBP", null);
        assertThrows(IllegalArgumentException.class, () -> service.createTransaction(input));
    }

    @Test
    void create_invalidCurrency_throws400() {
        AccountEntity acc = account("acc-1", "usr-1", "GBP", "100");
        when(accountDao.findByNumber("acc-1")).thenReturn(Optional.of(acc));

        TransactionBO input = txBO("usr-1", "acc-1", "deposit", "10", "USD", null);
        assertThrows(IllegalArgumentException.class, () -> service.createTransaction(input));
    }

    @Test
    void create_invalidType_throws400() {
        AccountEntity acc = account("acc-1", "usr-1", "GBP", "100");
        when(accountDao.findByNumber("acc-1")).thenReturn(Optional.of(acc));

        TransactionBO input = txBO("usr-1", "acc-1", "transfer", "10", "GBP", null);
        assertThrows(IllegalArgumentException.class, () -> service.createTransaction(input));
    }

    /* --------------------- listTransactions --------------------- */

    @Test
    void listTransactions_ok_mapsEntities() {
        AccountEntity acc = account("acc-1", "usr-1", "GBP", "100");
        when(accountDao.findByNumber("acc-1")).thenReturn(Optional.of(acc));

        TransactionEntity t1 = txEntity("tan-1", "acc-1", "usr-1", "5.50", "GBP", "deposit");
        when(txDao.findByAccountNumber("acc-1")).thenReturn(List.of(t1));

        var list = service.listTransactions("usr-1", "acc-1");
        assertEquals(1, list.size());
        assertEquals("tan-1", list.get(0).getId());
        assertEquals(new BigDecimal("5.50"), list.get(0).getAmount());
        assertEquals("deposit", list.get(0).getType());
    }

    @Test
    void listTransactions_accountMissing_throws404() {
        when(accountDao.findByNumber("acc-x")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.listTransactions("usr-1", "acc-x"));
    }

    @Test
    void listTransactions_notOwner_throws403() {
        AccountEntity acc = account("acc-1", "owner-1", "GBP", "100");
        when(accountDao.findByNumber("acc-1")).thenReturn(Optional.of(acc));
        assertThrows(ForbiddenException.class, () -> service.listTransactions("usr-1", "acc-1"));
    }

    /* --------------------- getTransactionForUser --------------------- */

    @Test
    void getTransactionForUser_ok() {
        AccountEntity acc = account("acc-1", "usr-1", "GBP", "100");
        when(accountDao.findByNumber("acc-1")).thenReturn(Optional.of(acc));

        TransactionEntity t = txEntity("tan-9", "acc-1", "usr-1", "20", "GBP", "withdrawal");
        when(txDao.findById("tan-9")).thenReturn(Optional.of(t));

        TransactionBO bo = service.getTransactionForUser("usr-1", "acc-1", "tan-9");

        assertEquals("tan-9", bo.getId());
        assertEquals("withdrawal", bo.getType());
        assertEquals(new BigDecimal("20"), bo.getAmount());
        assertEquals("usr-1", bo.getUserId());
        assertEquals("acc-1", bo.getAccountNumber());
    }

    @Test
    void getTransactionForUser_accountMissing_throws404() {
        when(accountDao.findByNumber("acc-1")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getTransactionForUser("usr-1", "acc-1", "tan-1"));
    }

    @Test
    void getTransactionForUser_txMissing_throws404() {
        AccountEntity acc = account("acc-1", "usr-1", "GBP", "100");
        when(accountDao.findByNumber("acc-1")).thenReturn(Optional.of(acc));
        when(txDao.findById("tan-1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getTransactionForUser("usr-1", "acc-1", "tan-1"));
    }

    @Test
    void getTransactionForUser_txBelongsToDifferentAccount_throws403() {
        AccountEntity acc = account("acc-1", "usr-1", "GBP", "100");
        when(accountDao.findByNumber("acc-1")).thenReturn(Optional.of(acc));

        // tx is for a DIFFERENT account
        TransactionEntity t = txEntity("tan-2", "acc-2", "usr-1", "10", "GBP", "deposit");
        when(txDao.findById("tan-2")).thenReturn(Optional.of(t));

        assertThrows(ForbiddenException.class, () -> service.getTransactionForUser("usr-1", "acc-1", "tan-2"));
    }

    /* --------------------- helpers --------------------- */

    private static AccountEntity account(String number, String ownerUserId, String currency, String balance) {
        AccountEntity a = new AccountEntity();
        a.setAccountNumber(number);
        a.setUserId(ownerUserId);
        a.setCurrency(currency);
        a.setBalance(new BigDecimal(balance));
        a.setCreated(OffsetDateTime.now().minusDays(1));
        a.setUpdated(OffsetDateTime.now().minusHours(1));
        return a;
    }

    private static TransactionBO txBO(String userId, String accountNumber, String type,
                                      String amountStr, String currency, String ref) {
        BigDecimal amt = amountStr == null ? null : new BigDecimal(amountStr);
        return TransactionBO.builder()
                .id(null) // service generates
                .userId(userId)
                .accountNumber(accountNumber)
                .type(type)
                .amount(amt)
                .currency(currency)
                .reference(ref)
                .created(null) // service sets
                .build();
    }

    private static TransactionEntity txEntity(String id, String accountNumber, String userId,
                                              String amountStr, String currency, String type) {
        return TransactionEntity.builder()
                .id(id)
                .accountNumber(accountNumber)
                .userId(userId)
                .amount(new BigDecimal(amountStr))
                .currency(currency)
                .type(type)
                .reference(null)
                .created(OffsetDateTime.now())
                .build();
    }
}
