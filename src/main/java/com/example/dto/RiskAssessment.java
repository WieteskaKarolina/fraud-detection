package com.example.dto;

import java.util.List;

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
