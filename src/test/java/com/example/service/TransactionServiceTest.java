package com.example.service;

import com.example.model.Transaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class TransactionServiceTest {

    @Inject
    TransactionService transactionService;

    @Test
    @Transactional
    void testSaveTransaction() {
        String requestId = "req-123";
        String bin = "123456";
        double amount = 100.50;
        String location = "New York";
        int riskScore = 75;
        String riskReason = "Suspicious activity";

        Transaction result = transactionService.saveTransaction(requestId, bin, amount, location, riskScore, riskReason);

        assertNotNull(result);
        assertEquals(requestId, result.requestId);
        assertEquals(bin, result.bin);
        assertEquals(amount, result.amount, 0.01);
        assertEquals(location, result.location);
        assertEquals(riskScore, result.riskScore);
        assertEquals(riskReason, result.riskReason);
    }
}
