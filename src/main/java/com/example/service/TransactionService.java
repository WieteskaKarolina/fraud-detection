package com.example.service;

import com.example.model.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TransactionService {

    @Transactional
    public Transaction saveTransaction(String requestId, String bin, double amount, String location, int riskScore, String riskReason) {
        Transaction transaction = new Transaction();
        transaction.setRequestId(requestId);
        transaction.setBin(bin);
        transaction.setAmount(amount);
        transaction.setLocation(location);
        transaction.setRiskScore(riskScore);
        transaction.setRiskReason(riskReason);
        transaction.persist();

        return transaction;
    }
}
