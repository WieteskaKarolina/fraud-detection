package com.example.service;

import com.example.dto.BinResponse;
import com.example.dto.RiskAssessment;
import com.example.dto.RiskFactor;
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

    private boolean hasIssuerCountryMismatch(
            BinResponse binData,
            TransactionRequest transactionRequest
    ) {
        if (binData == null || binData.getCountry() == null || transactionRequest == null) {
            return false;
        }

        String issuerCountry = normalizeCountryCode(binData.getCountry().getAlpha3());
        String transactionCountry = normalizeCountryCode(transactionRequest.getCountryCode());

        return issuerCountry != null
                && transactionCountry != null
                && !issuerCountry.equals(transactionCountry);
    }

    private String normalizeCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return null;
        }

        return countryCode.trim().toUpperCase(Locale.ROOT);
    }

}
