package com.example.repository;

import com.example.model.Transaction;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransactionRepository implements PanacheRepository<Transaction> {

    public Transaction saveTransaction(Transaction transaction) {
        this.persist(transaction);
        return transaction;
    }

    public Transaction findByBin(String bin) {
        return find("bin", bin).firstResult();
    }

    public Transaction findById(Long id) {
        return find("id", id).firstResult();
    }
}
