package com.example.service;

import com.example.model.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TransactionService {

    @Transactional
    public Transaction saveTransaction(String requestId, String bin, double amount, String location, int riskScore, String riskReason) {
        Transaction transaction = new Transaction();
        transaction.requestId = requestId;
        transaction.bin = bin;
        transaction.amount = amount;
        transaction.location = location;
        transaction.riskScore = riskScore;
        transaction.riskReason = riskReason;
        transaction.persist();

        return transaction;
    }
}
