package com.example.dto;

import java.util.List;

public record TransactionRiskResponse(
        String requestId,
        int riskScore,
        String explanation,
        List<RiskFactor> riskFactors
) {
    public TransactionRiskResponse {
        riskFactors = List.copyOf(riskFactors);
    }
}
