package com.example.facade;

import com.example.dto.BinResponse;
import com.example.dto.TransactionRequest;
import com.example.model.Transaction;
import com.example.service.BinLookupService;
import com.example.service.FraudDetectionService;
import com.example.service.TransactionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class TransactionFacade {

    private static final Logger LOGGER = Logger.getLogger(TransactionFacade.class);

    @Inject
    FraudDetectionService fraudDetectionService;

    @Inject
    BinLookupService binLookupService;

    @Inject
    TransactionService transactionService;

    public Response evaluateTransaction(TransactionRequest transactionRequest, String requestId, String username) {
        String traceId = (requestId != null) ? requestId : UUID.randomUUID().toString();
        LOGGER.info("User " + username + " evaluating transaction risk with X-Request-Id: " + traceId);

        try {
            BinResponse binResponse = binLookupService.lookupBin(transactionRequest.getBin())
                    .orElseThrow(() -> new WebApplicationException(
                            Response.status(Response.Status.NOT_FOUND)
                                    .entity(Map.of("error", "BIN details not found"))
                                    .build()));

            FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);

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

        } catch (WebApplicationException e) {
            return e.getResponse();
        } catch (Exception e) {
            LOGGER.error("Error evaluating transaction risk", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error evaluating transaction risk"))
                    .build();
        }
    }
}
