package com.example.service;

import com.example.dto.RiskAssessment;
import com.example.dto.BinResponse;
import com.example.dto.RiskFactor;
import com.example.dto.TransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionRiskEvaluatorTest {

    private TransactionRiskEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new TransactionRiskEvaluator();
    }

    @Test
    void evaluate_shouldReturnNoFactorsForOrdinaryTransaction() {
        RiskAssessment assessment = evaluator.evaluate(
                new BinResponse(),
                new TransactionRequest("585240", new BigDecimal("100.00"), "New York", "USA")
        );

        assertEquals(0, assessment.riskScore());
        assertEquals(List.of(), assessment.factors());
        assertEquals("No risk factors identified", assessment.explanation());
    }

    @Test
    void evaluate_shouldAddHighAmountFactor() {
        RiskAssessment assessment = evaluator.evaluate(
                new BinResponse(),
                new TransactionRequest("585240", new BigDecimal("1000.01"), "New York", "USA")
        );

        assertEquals(20, assessment.riskScore());
        assertEquals("HIGH_AMOUNT", assessment.factors().get(0).code());
    }

    @Test
    void evaluate_shouldAddCountryMismatchFactor() {
        BinResponse binResponse = new BinResponse();
        binResponse.getCountry().setAlpha3("POL");
        TransactionRequest request = new TransactionRequest(
                "585240", BigDecimal.ZERO, "New York", "USA"
        );

        RiskAssessment assessment = evaluator.evaluate(binResponse, request);

        assertEquals(25, assessment.riskScore());
        assertEquals("ISSUER_COUNTRY_MISMATCH", assessment.factors().get(0).code());
    }

    @Test
    void evaluate_shouldNotAddCountryMismatchFactorWhenCountriesMatch() {
        BinResponse binResponse = new BinResponse();
        binResponse.getCountry().setAlpha3("usa");
        TransactionRequest request = new TransactionRequest(
                "585240", BigDecimal.ZERO, "New York", "USA"
        );

        RiskAssessment assessment = evaluator.evaluate(binResponse, request);

        assertEquals(0, assessment.riskScore());
        assertEquals(List.of(), assessment.factors());
    }
}
