package com.example.facade;

import com.example.dto.BinResponse;
import com.example.dto.TransactionRequest;
import com.example.exception.ApiException;
import com.example.model.Transaction;
import com.example.service.BinLookupService;
import com.example.service.TransactionRiskEvaluator;
import com.example.service.TransactionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.Map;

@ApplicationScoped
public class TransactionFacade {

    private static final Logger LOGGER = Logger.getLogger(TransactionFacade.class);

    private final TransactionRiskEvaluator transactionRiskEvaluator;
    private final BinLookupService binLookupService;
    private final TransactionService transactionService;

    @Inject
    public TransactionFacade(
            TransactionRiskEvaluator transactionRiskEvaluator,
            BinLookupService binLookupService,
            TransactionService transactionService) {
        this.transactionRiskEvaluator = transactionRiskEvaluator;
        this.binLookupService = binLookupService;
        this.transactionService = transactionService;
    }

    public Response evaluateTransaction(
            TransactionRequest transactionRequest,
            String requestId,
            String correlationId,
            String username
    ) {
        LOGGER.infof("User %s evaluating transaction risk", username);

        BinResponse binResponse = binLookupService.lookupBin(transactionRequest.getBin())
                .orElseThrow(() -> new ApiException(Response.Status.NOT_FOUND, "BIN_NOT_FOUND",
                        "BIN details not found", transactionRequest.getBin()));

        TransactionRiskEvaluator.RiskAssessment assessment = transactionRiskEvaluator.evaluate(
                binResponse,
                transactionRequest
        );

        Transaction transaction = transactionService.saveTransaction(
                requestId,
                transactionRequest.getBin(),
                transactionRequest.getAmount(),
                transactionRequest.getLocation(),
                transactionRequest.getCountryCode(),
                assessment.riskScore(),
                assessment.explanation()
        );

        return Response.ok(Map.of(
                "requestId", requestId,
                "riskScore", transaction.getRiskScore(),
                "explanation", transaction.getRiskReason(),
                "riskFactors", assessment.factors().stream()
                        .map(factor -> Map.of(
                                "code", factor.code(),
                                "points", factor.points(),
                                "description", factor.description()
                        ))
                        .toList()
        )).build();
    }
}
