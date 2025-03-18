package com.example.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String bin;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private int riskScore;

    @Column(nullable = false)
    private String riskReason;

    @Column(nullable = false, unique = true)
    private String requestId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    public void setTimestamp() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}
