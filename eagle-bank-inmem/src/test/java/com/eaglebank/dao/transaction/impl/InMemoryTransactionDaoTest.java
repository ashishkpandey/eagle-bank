package com.eaglebank.dao.transaction.impl;

import com.eaglebank.domain.TransactionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTransactionDaoTest {

    private InMemoryTransactionDao dao;

    @BeforeEach
    void setup() {
        dao = new InMemoryTransactionDao();
    }

    private TransactionEntity tx(String id, String acc, BigDecimal amt, String type) {
        TransactionEntity t = new TransactionEntity();
        t.setId(id);
        t.setAccountNumber(acc);
        t.setUserId("u1");
        t.setAmount(amt);
        t.setCurrency("GBP");
        t.setType(type);
        t.setReference("ref-" + id);
        t.setCreated(OffsetDateTime.now());
        return t;
    }

    @Test
    void save_and_findById() {
        TransactionEntity t1 = tx("t1", "a1", new BigDecimal("100"), "deposit");
        dao.save(t1);

        Optional<TransactionEntity> found = dao.findById("t1");
        assertTrue(found.isPresent());
        assertEquals("a1", found.get().getAccountNumber());
        assertEquals(new BigDecimal("100"), found.get().getAmount());
    }

    @Test
    void findById_returnsEmptyIfMissing() {
        assertTrue(dao.findById("missing").isEmpty());
    }

    @Test
    void findByAccountNumber_returnsOnlyForThatAccount() {
        dao.save(tx("t1", "a1", new BigDecimal("10"), "deposit"));
        dao.save(tx("t2", "a2", new BigDecimal("20"), "deposit"));
        dao.save(tx("t3", "a1", new BigDecimal("30"), "withdrawal"));

        List<TransactionEntity> list = dao.findByAccountNumber("a1");

        assertEquals(2, list.size());
        assertTrue(list.stream().allMatch(t -> "a1".equals(t.getAccountNumber())));
    }

    @Test
    void findByAccountNumber_returnsMostRecentFirst() {
        dao.save(tx("t1", "a1", new BigDecimal("10"), "deposit"));
        dao.save(tx("t2", "a1", new BigDecimal("20"), "deposit"));
        dao.save(tx("t3", "a1", new BigDecimal("30"), "withdrawal"));

        List<TransactionEntity> list = dao.findByAccountNumber("a1");

        // Because we insert new IDs at the head
        assertEquals("t3", list.get(0).getId());
        assertEquals("t2", list.get(1).getId());
        assertEquals("t1", list.get(2).getId());
    }

    @Test
    void save_throwsIfMissingRequiredFields() {
        TransactionEntity t = new TransactionEntity();
        assertThrows(NullPointerException.class, () -> dao.save(t));
    }
}
