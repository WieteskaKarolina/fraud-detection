package com.example.service;

import com.example.dto.BinResponse;
import com.example.dto.TransactionRequest;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class TransactionRiskEvaluator {

    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = BigDecimal.valueOf(1_000);
    private static final int HIGH_AMOUNT_POINTS = 20;
    private static final int ANONYMOUS_PREPAID_POINTS = 40;
    private static final int ISSUER_COUNTRY_MISMATCH_POINTS = 25;
    private static final int MAX_RISK_SCORE = 100;

    public RiskAssessment evaluate(BinResponse binData, TransactionRequest transactionRequest) {
        List<RiskFactor> factors = new ArrayList<>();

        if (transactionRequest.getAmount() != null
                && transactionRequest.getAmount().compareTo(HIGH_AMOUNT_THRESHOLD) > 0) {
            factors.add(new RiskFactor(
                    "HIGH_AMOUNT",
                    HIGH_AMOUNT_POINTS,
                    "Transaction amount exceeds 1000"
            ));
        }

        if (binData != null && "Y".equalsIgnoreCase(binData.getAnonymousPrepaidIndicator())) {
            factors.add(new RiskFactor(
                    "ANONYMOUS_PREPAID_BIN",
                    ANONYMOUS_PREPAID_POINTS,
                    "BIN is identified as an anonymous prepaid card"
            ));
        }

        if (hasIssuerCountryMismatch(binData, transactionRequest)) {
            factors.add(new RiskFactor(
                    "ISSUER_COUNTRY_MISMATCH",
                    ISSUER_COUNTRY_MISMATCH_POINTS,
                    "Transaction country differs from the card issuer country"
            ));
        }

        int score = factors.stream().mapToInt(RiskFactor::points).sum();
        return new RiskAssessment(Math.min(score, MAX_RISK_SCORE), factors);
    }

    private boolean hasIssuerCountryMismatch(BinResponse binData, TransactionRequest transactionRequest) {
        if (binData == null || binData.getCountry() == null
                || transactionRequest.getCountryCode() == null
                || binData.getCountry().getAlpha3() == null
                || binData.getCountry().getAlpha3().isBlank()) {
            return false;
        }

        String issuerCountry = binData.getCountry().getAlpha3().trim();
        String transactionCountry = transactionRequest.getCountryCode().trim().toUpperCase(Locale.ROOT);
        return !issuerCountry.equalsIgnoreCase(transactionCountry);
    }

    public record RiskFactor(String code, int points, String description) {
    }

    public record RiskAssessment(int riskScore, List<RiskFactor> factors) {
        public RiskAssessment {
            factors = List.copyOf(factors);
        }

        public String explanation() {
            if (factors.isEmpty()) {
                return "No risk factors identified";
            }
            return String.join("; ", factors.stream().map(RiskFactor::description).toList());
        }
    }
}
