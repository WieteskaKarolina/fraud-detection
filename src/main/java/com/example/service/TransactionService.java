package com.example.service;

import com.example.model.Transaction;
import com.example.repository.TransactionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;

@ApplicationScoped
public class TransactionService {

    private final TransactionRepository repository;

    @Inject
    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Transaction saveTransaction(
            String requestId,
            String bin,
            BigDecimal amount,
            String location,
            String countryCode,
            int riskScore,
            String riskReason) {

        Transaction transaction = new Transaction();
        transaction.setRequestId(requestId);
        transaction.setBin(bin);
        transaction.setAmount(amount);
        transaction.setLocation(location);
        transaction.setCountryCode(countryCode);
        transaction.setRiskScore(riskScore);
        transaction.setRiskReason(riskReason);
        return repository.saveTransaction(transaction);
    }
}
