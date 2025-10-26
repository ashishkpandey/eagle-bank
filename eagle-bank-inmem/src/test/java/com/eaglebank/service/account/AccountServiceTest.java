package com.eaglebank.service.account;

import com.eaglebank.bo.AccountBO;
import com.eaglebank.dao.account.AccountDao;
import com.eaglebank.domain.AccountEntity;
import com.eaglebank.exception.ForbiddenException;
import com.eaglebank.exception.NotFoundException;
import com.eaglebank.mapper.account.AccountEntityMapper;
import com.eaglebank.testutil.MockData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    private AccountDao dao;
    private AccountEntityMapper mapper;
    private AccountService svc;

    @BeforeEach
    void setup() {
        dao = mock(AccountDao.class);
        mapper = new AccountEntityMapper();
        svc = new AccountService(dao, mapper);
    }

    @Test
    void getAccountForUser_happyPath() {
        AccountEntity e = MockData.accountEntity("u1", "a1", new BigDecimal("100"), "GBP");
        when(dao.findByNumber("a1")).thenReturn(Optional.of(e));

        AccountBO bo = svc.getAccountForUser("u1", "a1");

        assertEquals("a1", bo.getAccountNumber());
        assertEquals("u1", bo.getOwnerUserId());
    }

    @Test
    void getAccountForUser_404IfMissing() {
        when(dao.findByNumber("x")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> svc.getAccountForUser("u1", "x"));
    }

    @Test
    void getAccountForUser_403IfNotOwner() {
        AccountEntity e = MockData.accountEntity("u2", "a1", new BigDecimal("100"), "GBP");
        when(dao.findByNumber("a1")).thenReturn(Optional.of(e));
        assertThrows(ForbiddenException.class, () -> svc.getAccountForUser("u1", "a1"));
    }

    @Test
    void updateAccountName_updatesNameAndSaves() {
        AccountEntity e = MockData.accountEntity("u1", "a1", new BigDecimal("100"), "GBP");
        when(dao.findByNumber("a1")).thenReturn(Optional.of(e));

        svc.updateAccountName("u1", "a1", "My Savings");

        verify(dao).save(argThat(acc ->
                "My Savings".equals(acc.getName()) &&
                        "a1".equals(acc.getAccountNumber())));
    }

    @Test
    void deleteAccount_happyPath() {
        AccountEntity e = MockData.accountEntity("u1", "a1", new BigDecimal("100"), "GBP");
        when(dao.findByNumber("a1")).thenReturn(Optional.of(e));

        svc.deleteAccount("u1", "a1");

        verify(dao).deleteByNumber("a1");
    }

    @Test
    void listUserAccounts_returnsMappedList() {
        AccountEntity a1 = MockData.accountEntity("u1", "a1", new BigDecimal("100"), "GBP");
        AccountEntity a2 = MockData.accountEntity("u1", "a2", new BigDecimal("200"), "GBP");
        when(dao.findByUserId("u1")).thenReturn(List.of(a1, a2));

        List<AccountBO> list = svc.listUserAccounts("u1");

        assertEquals(2, list.size());
        assertTrue(list.stream().anyMatch(a -> a.getAccountNumber().equals("a1")));
        assertTrue(list.stream().anyMatch(a -> a.getAccountNumber().equals("a2")));
    }
}
