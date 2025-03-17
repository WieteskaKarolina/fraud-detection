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
        transaction.requestId = UUID.randomUUID().toString();
        transaction.bin = "123456";
        transaction.amount = 100.50;
        transaction.location = "New York";
        transaction.riskScore = 75;
        transaction.riskReason = "Suspicious activity";

        Transaction savedTransaction = transactionRepository.saveTransaction(transaction);

        assertNotNull(savedTransaction.id, "Transaction ID should be generated");
        assertEquals(transaction.requestId, savedTransaction.requestId);
    }


    @Test
    @Transactional
    void testFindByBin() {
        Transaction transaction = new Transaction();
        transaction.requestId = "req-456";
        transaction.bin = "654321";
        transaction.amount = 200.75;
        transaction.location = "Los Angeles";
        transaction.riskScore = 50;
        transaction.riskReason = "Low fraud risk";

        transactionRepository.saveTransaction(transaction);

        Transaction foundTransaction = transactionRepository.findByBin("654321");

        assertNotNull(foundTransaction);
        assertEquals("req-456", foundTransaction.requestId);
        assertEquals("654321", foundTransaction.bin);
    }

    @Test
    @Transactional
    void testFindById() {
        Transaction transaction = new Transaction();
        transaction.requestId = UUID.randomUUID().toString();
        transaction.bin = "987654";
        transaction.amount = 300.00;
        transaction.location = "Chicago";
        transaction.riskScore = 30;
        transaction.riskReason = "Moderate fraud risk";

        transactionRepository.saveTransaction(transaction);
        entityManager.flush();
        entityManager.refresh(transaction);

        Transaction foundTransaction = transactionRepository.findById(transaction.id);

        assertNotNull(foundTransaction, "Transaction should be found in the database");
        assertNotNull(foundTransaction.id, "Transaction ID should not be null");
        assertEquals(transaction.requestId, foundTransaction.requestId);
        assertEquals("987654", foundTransaction.bin);
    }

}
