package com.example.facade;

import com.example.dto.BinResponse;
import com.example.dto.TransactionRequest;
import com.example.exception.ApiException;
import com.example.model.Transaction;
import com.example.service.BinLookupService;
import com.example.service.FraudDetectionService;
import com.example.service.TransactionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class TransactionFacade {

    private static final Logger LOGGER = Logger.getLogger(TransactionFacade.class);

    private final FraudDetectionService fraudDetectionService;
    private final BinLookupService binLookupService;
    private final TransactionService transactionService;

    @Inject
    public TransactionFacade(
            FraudDetectionService fraudDetectionService,
            BinLookupService binLookupService,
            TransactionService transactionService) {
        this.fraudDetectionService = fraudDetectionService;
        this.binLookupService = binLookupService;
        this.transactionService = transactionService;
    }

    public Response evaluateTransaction(TransactionRequest transactionRequest, String requestId, String username) {
        String traceId = (requestId != null) ? requestId : UUID.randomUUID().toString();
        LOGGER.info("User " + username + " evaluating transaction risk with X-Request-Id: " + traceId);

        BinResponse binResponse = binLookupService.lookupBin(transactionRequest.getBin())
                .orElseThrow(() -> new ApiException(Response.Status.NOT_FOUND, "BIN_NOT_FOUND",
                        "BIN details not found", transactionRequest.getBin()));

        FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(
                binResponse,
                transactionRequest
        );

        Transaction transaction = transactionService.saveTransaction(
                traceId,
                transactionRequest.getBin(),
                transactionRequest.getAmount(),
                transactionRequest.getLocation(),
                evaluation.getRiskScore(),
                evaluation.getDescription()
        );

        return Response.ok(Map.of(
                "requestId", traceId,
                "riskScore", transaction.getRiskScore(),
                "explanation", transaction.getRiskReason()
        )).header("X-Request-Id", traceId).build();
    }
}
