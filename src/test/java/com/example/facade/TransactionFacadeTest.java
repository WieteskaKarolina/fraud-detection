package com.example.facade;

import com.example.dto.BinResponse;
import com.example.dto.TransactionRequest;
import com.example.model.Transaction;
import com.example.service.BinLookupService;
import com.example.service.FraudDetectionService;
import com.example.service.TransactionService;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionFacadeTest {

    @InjectMocks
    private TransactionFacade transactionFacade;

    @Mock
    private FraudDetectionService fraudDetectionService;

    @Mock
    private BinLookupService binLookupService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private Logger logger;

    private TransactionRequest transactionRequest;
    private String requestId;
    private String username;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        transactionRequest = new TransactionRequest("585240", 100.0, "New York");
        requestId = UUID.randomUUID().toString();
        username = "testUser";
    }

    @Test
    void evaluateTransaction_ShouldReturnSuccessResponse() {
        BinResponse binResponse = new BinResponse();
        when(binLookupService.lookupBin(transactionRequest.getBin())).thenReturn(Optional.of(binResponse));

        FraudDetectionService.RiskEvaluation riskEvaluation = new FraudDetectionService.RiskEvaluation(50, "Test Risk");
        when(fraudDetectionService.evaluateRisk(binResponse, transactionRequest)).thenReturn(riskEvaluation);

        Transaction mockTransaction = new Transaction();
        mockTransaction.setRiskScore(50);
        mockTransaction.setRiskReason("Test Risk");
        when(transactionService.saveTransaction(any(), any(), anyDouble(), any(), anyInt(), any()))
                .thenReturn(mockTransaction);

        Response response = transactionFacade.evaluateTransaction(transactionRequest, requestId, username);

        assertEquals(200, response.getStatus());
        Map<String, Object> responseBody = (Map<String, Object>) response.getEntity();
        assertEquals(50, responseBody.get("riskScore"));
        assertEquals("Test Risk", responseBody.get("explanation"));

        verify(binLookupService).lookupBin(transactionRequest.getBin());
        verify(fraudDetectionService).evaluateRisk(binResponse, transactionRequest);
        verify(transactionService).saveTransaction(any(), any(), anyDouble(), any(), anyInt(), any());
    }

    @Test
    void evaluateTransaction_ShouldReturnNotFound_WhenBinNotFound() {
        when(binLookupService.lookupBin(transactionRequest.getBin())).thenReturn(Optional.empty());

        Response response = transactionFacade.evaluateTransaction(transactionRequest, requestId, username);

        assertEquals(404, response.getStatus());
        Map<String, Object> responseBody = (Map<String, Object>) response.getEntity();
        assertEquals("BIN details not found", responseBody.get("error"));

        verify(binLookupService).lookupBin(transactionRequest.getBin());
        verifyNoInteractions(fraudDetectionService, transactionService);
    }

    @Test
    void evaluateTransaction_ShouldReturnInternalServerError_OnException() {
        when(binLookupService.lookupBin(transactionRequest.getBin())).thenThrow(new RuntimeException("Unexpected error"));

        Response response = transactionFacade.evaluateTransaction(transactionRequest, requestId, username);

        assertEquals(500, response.getStatus());
        Map<String, Object> responseBody = (Map<String, Object>) response.getEntity();
        assertEquals("Error evaluating transaction risk", responseBody.get("error"));

        verify(binLookupService).lookupBin(transactionRequest.getBin());
        verifyNoInteractions(fraudDetectionService, transactionService);
    }
}
