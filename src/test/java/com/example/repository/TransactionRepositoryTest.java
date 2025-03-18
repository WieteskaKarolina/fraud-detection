package com.example.repository;

import com.example.model.Transaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class TransactionRepositoryTest {

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    EntityManager entityManager;

    @BeforeEach
    @Transactional
    void cleanDatabase() {
        transactionRepository.deleteAll();
    }

    @Test
    @Transactional
    void testSaveTransaction() {
        Transaction transaction = new Transaction();
        transaction.setRequestId(UUID.randomUUID().toString());
        transaction.setBin("123456");
        transaction.setAmount(100.50);
        transaction.setLocation("New York");
        transaction.setRiskScore(75);
        transaction.setRiskReason("Suspicious activity");

        Transaction savedTransaction = transactionRepository.saveTransaction(transaction);

        assertNotNull(savedTransaction.getId(), "Transaction ID should be generated");
        assertEquals(transaction.getRequestId(), savedTransaction.getRequestId());
    }


    @Test
    @Transactional
    void testFindByBin() {
        Transaction transaction = new Transaction();
        transaction.setRequestId("req-456");
        transaction.setBin("654321");
        transaction.setAmount(200.75);
        transaction.setLocation("Los Angeles");
        transaction.setRiskScore(50);
        transaction.setRiskReason("Low fraud risk");

        transactionRepository.saveTransaction(transaction);

        Transaction foundTransaction = transactionRepository.findByBin("654321");

        assertNotNull(foundTransaction);
        assertEquals("req-456", foundTransaction.getRequestId());
        assertEquals("654321", foundTransaction.getBin());
    }

    @Test
    @Transactional
    void testFindById() {
        Transaction transaction = new Transaction();
        transaction.setRequestId(UUID.randomUUID().toString());
        transaction.setBin("987654");
        transaction.setAmount(300.00);
        transaction.setLocation("Chicago");
        transaction.setRiskScore(30);
        transaction.setRiskReason("Moderate fraud risk");

        transactionRepository.saveTransaction(transaction);
        entityManager.flush();
        entityManager.refresh(transaction);

        Transaction foundTransaction = transactionRepository.findById(transaction.getId());

        assertNotNull(foundTransaction, "Transaction should be found in the database");
        assertNotNull(foundTransaction.getId(), "Transaction ID should not be null");
        assertEquals(transaction.getRequestId(), foundTransaction.getRequestId());
        assertEquals("987654", foundTransaction.getBin());
    }

}
