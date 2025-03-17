package com.example.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import java.time.LocalDateTime;
import jakarta.persistence.PrePersist;

@Entity
public class Transaction extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String bin;

    @Column(nullable = false)
    public double amount;

    @Column(nullable = false)
    public String location;

    @Column(nullable = false)
    public int riskScore;

    @Column(nullable = false)
    public String riskReason;

    @Column(nullable = false, unique = true)
    public String requestId;

    @Column(nullable = false)
    public LocalDateTime timestamp;

    @PrePersist
    public void setTimestamp() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}
