package com.eaglebank.dao.account;

import com.eaglebank.dao.account.impl.InMemoryAccountDao;
import com.eaglebank.domain.AccountEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryAccountDaoTest {

    private InMemoryAccountDao dao;

    @BeforeEach
    void setup() {
        dao = new InMemoryAccountDao();
    }

    private AccountEntity account(String userId, String number, BigDecimal balance) {
        AccountEntity a = new AccountEntity();
        a.setUserId(userId);
        a.setAccountNumber(number);
        a.setBalance(balance);
        a.setCurrency("GBP");
        return a;
    }

    @Test
    void save_and_findByNumber() {
        AccountEntity a = account("u1", "a1", new BigDecimal("100"));
        dao.save(a);

        Optional<AccountEntity> found = dao.findByNumber("a1");
        assertTrue(found.isPresent());
        assertEquals("u1", found.get().getUserId());
        assertEquals(new BigDecimal("100"), found.get().getBalance());
    }

    @Test
    void findByUserId_returnsOnlyMatchingAccounts() {
        dao.save(account("u1", "a1", new BigDecimal("50")));
        dao.save(account("u2", "a2", new BigDecimal("60")));
        dao.save(account("u1", "a3", new BigDecimal("70")));

        List<AccountEntity> list = dao.findByUserId("u1");
        assertEquals(2, list.size());
        assertTrue(list.stream().anyMatch(a -> a.getAccountNumber().equals("a1")));
        assertTrue(list.stream().anyMatch(a -> a.getAccountNumber().equals("a3")));
    }

    @Test
    void deleteByNumber_removesAccount() {
        dao.save(account("u1", "a1", new BigDecimal("200")));
        assertTrue(dao.findByNumber("a1").isPresent());

        dao.deleteByNumber("a1");

        assertFalse(dao.findByNumber("a1").isPresent());
    }

    @Test
    void findByNumber_returnsEmptyIfMissing() {
        assertTrue(dao.findByNumber("missing").isEmpty());
    }
}
