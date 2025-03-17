package com.example.service;

import com.example.dto.TransactionRequest;
import com.example.model.BinResponse;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;

@ApplicationScoped
public class FraudDetectionService {
    // Contains high risk countries: Russia, Nigeria, Venezuela
    private static final Set<Integer> HIGH_RISK_COUNTRIES = new HashSet<>(Arrays.asList(643, 566, 862));

    private final List<Rule<BinResponse>> binResponseRules = new ArrayList<>();
    private final List<Rule<TransactionRequest>> transactionRequestRules = new ArrayList<>();

    public FraudDetectionService() {
        binResponseRules.add(this::evaluateHighRiskCountry);
        binResponseRules.add(this::evaluateAnonymousPrepaidCard);
        binResponseRules.add(this::evaluateCorporateCardMisuse);
        binResponseRules.add(this::evaluateNonReloadableCard);
        binResponseRules.add(this::evaluateAuthorizationOnlyCard);
        binResponseRules.add(this::evaluateLocalUseMismatch);
        binResponseRules.add(this::evaluateGamblingBlock);
        binResponseRules.add(this::evaluateNoFundingSource);

        transactionRequestRules.add(this::evaluateHighTransactionAmount);
        transactionRequestRules.add(this::evaluateHighRiskLocation);
    }

    public RiskEvaluation evaluateRisk(BinResponse binData, TransactionRequest transactionRequest) {
        int totalRiskScore = 0;
        StringBuilder riskDescription = new StringBuilder("Risk factors:\n");

        for (Rule<BinResponse> rule : binResponseRules) {
            RiskEvaluation evaluation = rule.evaluate(binData);
            totalRiskScore += evaluation.getRiskScore();
            if (evaluation.getRiskScore() > 0) {
                riskDescription.append(evaluation.getDescription()).append("\n");
            }
        }

        for (Rule<TransactionRequest> rule : transactionRequestRules) {
            RiskEvaluation evaluation = rule.evaluate(transactionRequest);
            totalRiskScore += evaluation.getRiskScore();
            if (evaluation.getRiskScore() > 0) {
                riskDescription.append(evaluation.getDescription()).append("\n");
            }
        }

        totalRiskScore = Math.min(totalRiskScore, 100);

        return new RiskEvaluation(totalRiskScore, riskDescription.toString());
    }

    private RiskEvaluation evaluateHighRiskCountry(BinResponse binData) {
        if (HIGH_RISK_COUNTRIES.contains(binData.getCountry().getCode())) {
            return new RiskEvaluation(50, "High-risk country (Russia, Nigeria, Venezuela)");
        }
        return RiskEvaluation.NO_RISK;
    }

    private RiskEvaluation evaluateAnonymousPrepaidCard(BinResponse binData) {
        if ("Y".equalsIgnoreCase(binData.getAnonymousPrepaidIndicator())) {
            return new RiskEvaluation(40, "Anonymous prepaid card detected");
        }
        return RiskEvaluation.NO_RISK;
    }

    private RiskEvaluation evaluateCorporateCardMisuse(BinResponse binData) {
        if ("CORPORATE".equalsIgnoreCase(binData.getConsumerType())) {
            return new RiskEvaluation(30, "Corporate card misuse detected");
        }
        return RiskEvaluation.NO_RISK;
    }

    private RiskEvaluation evaluateNonReloadableCard(BinResponse binData) {
        if (binData.isNonReloadableIndicator()) {
            return new RiskEvaluation(20, "Non-reloadable card detected");
        }
        return RiskEvaluation.NO_RISK;
    }

    private RiskEvaluation evaluateAuthorizationOnlyCard(BinResponse binData) {
        if (binData.isAuthorizationOnly()) {
            return new RiskEvaluation(50, "Authorization-only card detected");
        }
        return RiskEvaluation.NO_RISK;
    }

    private RiskEvaluation evaluateLocalUseMismatch(BinResponse binData) {
        if (binData.isLocalUse()) {
            return new RiskEvaluation(25, "Local use mismatch detected");
        }
        return RiskEvaluation.NO_RISK;
    }

    private RiskEvaluation evaluateGamblingBlock(BinResponse binData) {
        if (binData.isGamblingBlockEnabled()) {
            return new RiskEvaluation(60, "Gambling block enabled");
        }
        return RiskEvaluation.NO_RISK;
    }

    private RiskEvaluation evaluateNoFundingSource(BinResponse binData) {
        if ("NONE".equalsIgnoreCase(binData.getFundingSource())) {
            return new RiskEvaluation(30, "No funding source detected");
        }
        return RiskEvaluation.NO_RISK;
    }

    private RiskEvaluation evaluateHighTransactionAmount(TransactionRequest transactionRequest) {
        if (transactionRequest.amount > 1000) {
            return new RiskEvaluation(20, "High transaction amount detected");
        }
        return RiskEvaluation.NO_RISK;
    }

    private RiskEvaluation evaluateHighRiskLocation(TransactionRequest transactionRequest) {
        if ("HighRiskCity".equalsIgnoreCase(transactionRequest.location)) {
            return new RiskEvaluation(30, "High-risk location detected");
        }
        return RiskEvaluation.NO_RISK;
    }

    public interface Rule<T> {
        RiskEvaluation evaluate(T data);
    }

    public static class RiskEvaluation {
        public static final RiskEvaluation NO_RISK = new RiskEvaluation(0, "");

        private final int riskScore;
        private final String description;

        public RiskEvaluation(int riskScore, String description) {
            this.riskScore = riskScore;
            this.description = description;
        }

        public int getRiskScore() {
            return riskScore;
        }

        public String getDescription() {
            return description;
        }
    }
}
