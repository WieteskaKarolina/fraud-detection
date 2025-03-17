package com.example.controller;

import com.example.dto.TransactionRequest;
import com.example.model.BinResponse;
import com.example.model.Transaction;
import com.example.service.BinLookupService;
import com.example.service.FraudDetectionService;
import com.example.service.TransactionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.UUID;


@ApplicationScoped
@Path("/api/transactions")
public class TransactionRiskController {

    private static final Logger LOGGER = Logger.getLogger(TransactionRiskController.class);

    @Inject
    FraudDetectionService fraudDetectionService;

    @Inject
    BinLookupService binLookupService;

    @Inject
    TransactionService transactionService;


    @POST
    @Path("/evaluate")
    @Operation(summary = "Evaluate transaction risk", description = "Evaluates transaction risk based on given details")
    @Consumes("application/json")
    @Produces("application/json")
    public Response evaluateTransactionRisk(@Valid @RequestBody TransactionRequest transactionRequest,
                                            @HeaderParam("X-Request-Id") String requestId) {
        String traceId = (requestId != null) ? requestId : UUID.randomUUID().toString();
        LOGGER.info("Evaluating transaction risk with X-Request-Id: " + traceId);

        try {
            String bin = transactionRequest.getBin();
            BinResponse binResponse = binLookupService.lookupBin(bin)
                    .orElseThrow(() -> new WebApplicationException(
                            Response.status(Response.Status.NOT_FOUND)
                                    .entity(Map.of("error", "BIN details not found"))
                                    .build()));

            FraudDetectionService.RiskEvaluation evaluation = fraudDetectionService.evaluateRisk(binResponse, transactionRequest);

            Transaction transaction = transactionService.saveTransaction(
                    traceId,
                    bin,
                    transactionRequest.amount,
                    transactionRequest.location,
                    evaluation.getRiskScore(),
                    evaluation.getDescription()
            );

            return Response.ok(Map.of(
                    "requestId", traceId,
                    "riskScore", transaction.riskScore,
                    "explanation", transaction.riskReason
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


    @GET
    @Path("/bin/{binNumber}")
    @Produces("application/json")
    @Operation(summary = "Retrieve BIN details", description = "Fetches BIN details for the provided BIN number.")
    public Response getBinDetails(@PathParam("binNumber")
                                  @Pattern(regexp = "\\d{6,11}", message = "BIN must be 6 to 11 digits") String binNumber) {
        LOGGER.info("Fetching BIN details for: " + binNumber);

        try {
            BinResponse binResponse = binLookupService.lookupBin(binNumber)
                    .orElseThrow(() -> new WebApplicationException("BIN details not found", Response.Status.NOT_FOUND));

            return Response.ok(binResponse).build();
        } catch (Exception e) {
            LOGGER.error("Error fetching BIN details: {}", e.getMessage(), e);
            throw new WebApplicationException("Error fetching BIN details", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
