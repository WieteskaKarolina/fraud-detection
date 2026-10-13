package com.example.service;

import com.example.model.Transaction;
import com.example.repository.TransactionRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class TransactionServiceTest {

    @Inject
    TransactionService transactionService;

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    EntityManager entityManager;

    @Test
    @Transactional
    void testSaveTransaction() {
        String requestId = "req-123";
        String bin = "123456";
        BigDecimal amount = new BigDecimal("100.50");
        String location = "New York";
        String countryCode = "USA";
        int riskScore = 75;
        String riskReason = "Suspicious activity";

        Transaction result = transactionService.saveTransaction(
                requestId,
                bin,
                amount,
                location,
                countryCode,
                riskScore,
                riskReason
        );

        assertNotNull(result);
        assertNotNull(result.getId(), "The persisted transaction should have an ID");

        entityManager.flush();
        entityManager.clear();

        Transaction persisted = transactionRepository.findById(result.getId());

        assertNotNull(persisted, "The transaction should be persisted in the database");
        assertAll(
                () -> assertEquals(requestId, persisted.getRequestId()),
                () -> assertEquals(bin, persisted.getBin()),
                () -> assertEquals(0, amount.compareTo(persisted.getAmount())),
                () -> assertEquals(location, persisted.getLocation()),
                () -> assertEquals(countryCode, persisted.getCountryCode()),
                () -> assertEquals(riskScore, persisted.getRiskScore()),
                () -> assertEquals(riskReason, persisted.getRiskReason()),
                () -> assertNotNull(persisted.getTimestamp())
        );
    }
}
