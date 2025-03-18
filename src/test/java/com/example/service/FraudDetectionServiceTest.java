package com.example.service;

import com.example.model.BinResponse;
import com.example.dto.TransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FraudDetectionServiceTest {

    private FraudDetectionService fraudDetectionService;
    private BinResponse binResponse;
    private TransactionRequest transactionRequest;

    private static final int RUSSIA_COUNTRY_CODE = 643;
    private static final int POLAND_COUNTRY_CODE = 616;
    private static final int NIGERIA_COUNTRY_CODE = 566;
    private static final int VENEZUELA_COUNTRY_CODE = 862;

    @BeforeEach
    public void setUp() {
        fraudDetectionService = new FraudDetectionService();
        binResponse = new BinResponse();
        transactionRequest = new TransactionRequest();
    }

    @Test
    public void testCombinedRisk() {
        binResponse.getCountry().setCode(RUSSIA_COUNTRY_CODE);
        binResponse.setAnonymousPrepaidIndicator("Y");
        binResponse.setConsumerType("CORPORATE");
        transactionRequest.setAmount(2000);
        transactionRequest.setLocation("HighRiskCity");

        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);
        assertEquals(100, evaluation.getRiskScore());
        assertEquals("Risk factors:\nHigh-risk country (Russia, Nigeria, Venezuela)\nAnonymous prepaid card detected\nCorporate card misuse detected\nHigh transaction amount detected\nHigh-risk location detected\n",
                evaluation.getDescription());
    }

    @Test
    public void testHighRiskCountryRule() {
        binResponse.getCountry().setCode(RUSSIA_COUNTRY_CODE);
        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);
        assertEquals(50, evaluation.getRiskScore());
        assertEquals("Risk factors:\nHigh-risk country (Russia, Nigeria, Venezuela)\n", evaluation.getDescription());
    }

    @Test
    public void testNonHighRiskCountry() {
        binResponse.getCountry().setCode(POLAND_COUNTRY_CODE);
        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);
        assertEquals(0, evaluation.getRiskScore());
        assertEquals("Risk factors:\n", evaluation.getDescription());
    }

    @Test
    public void testAnonymousPrepaidCardRule() {
        binResponse.setAnonymousPrepaidIndicator("Y");
        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);
        assertEquals(40, evaluation.getRiskScore());
        assertEquals("Risk factors:\nAnonymous prepaid card detected\n", evaluation.getDescription());
    }

    @Test
    public void testNonAnonymousPrepaidCardRule() {
        binResponse.setAnonymousPrepaidIndicator("N");
        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);
        assertEquals(0, evaluation.getRiskScore());
        assertEquals("Risk factors:\n", evaluation.getDescription());
    }

    @Test
    public void testCorporateCardMisuseRule() {
        binResponse.setConsumerType("CORPORATE");
        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);
        assertEquals(30, evaluation.getRiskScore());
        assertEquals("Risk factors:\nCorporate card misuse detected\n", evaluation.getDescription());
    }

    @Test
    public void testNonCorporateCardMisuseRule() {
        binResponse.setConsumerType("PERSONAL");
        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);
        assertEquals(0, evaluation.getRiskScore());
        assertEquals("Risk factors:\n", evaluation.getDescription());
    }

    @Test
    public void testNonReloadableCardRule() {
        binResponse.setNonReloadableIndicator(true);
        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);
        assertEquals(20, evaluation.getRiskScore());
        assertEquals("Risk factors:\nNon-reloadable card detected\n", evaluation.getDescription());
    }

    @Test
    public void testReloadableCardRule() {
        binResponse.setNonReloadableIndicator(false);
        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);
        assertEquals(0, evaluation.getRiskScore());
        assertEquals("Risk factors:\n", evaluation.getDescription());
    }

    @Test
    public void testAuthorizationOnlyCardRule() {
        binResponse.setAuthorizationOnly(true);
        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);
        assertEquals(50, evaluation.getRiskScore());
        assertEquals("Risk factors:\nAuthorization-only card detected\n", evaluation.getDescription());
    }

    @Test
    public void testNonAuthorizationOnlyCardRule() {
        binResponse.setAuthorizationOnly(false);
        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);
        assertEquals(0, evaluation.getRiskScore());
        assertEquals("Risk factors:\n", evaluation.getDescription());
    }

    @Test
    public void testLocalUseMismatchRule() {
        binResponse.setLocalUse(true);
        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);
        assertEquals(25, evaluation.getRiskScore());
        assertEquals("Risk factors:\nLocal use mismatch detected\n", evaluation.getDescription());
    }

    @Test
    public void testNonLocalUseMismatchRule() {
        binResponse.setLocalUse(false);
        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);
        assertEquals(0, evaluation.getRiskScore());
        assertEquals("Risk factors:\n", evaluation.getDescription());
    }

    @Test
    public void testGamblingBlockRule() {
        binResponse.setGamblingBlockEnabled(true);
        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);
        assertEquals(60, evaluation.getRiskScore());
        assertEquals("Risk factors:\nGambling block enabled\n", evaluation.getDescription());
    }

    @Test
    public void testNonGamblingBlockRule() {
        binResponse.setGamblingBlockEnabled(false);
        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);
        assertEquals(0, evaluation.getRiskScore());
        assertEquals("Risk factors:\n", evaluation.getDescription());
    }

    @Test
    public void testNoFundingSourceRule() {
        binResponse.setFundingSource("NONE");
        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);
        assertEquals(30, evaluation.getRiskScore());
        assertEquals("Risk factors:\nNo funding source detected\n", evaluation.getDescription());
    }

    @Test
    public void testWithFundingSourceRule() {
        binResponse.setFundingSource("CREDIT");
        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);
        assertEquals(0, evaluation.getRiskScore());
        assertEquals("Risk factors:\n", evaluation.getDescription());
    }
}
