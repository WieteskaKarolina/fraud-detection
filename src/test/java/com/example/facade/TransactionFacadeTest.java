package com.example.facade;

import com.example.dto.BinResponse;
import com.example.dto.TransactionRequest;
import com.example.exception.ApiException;
import com.example.model.Transaction;
import com.example.service.BinLookupService;
import com.example.service.TransactionRiskEvaluator;
import com.example.service.TransactionService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
    private TransactionRiskEvaluator transactionRiskEvaluator;

    @Mock
    private BinLookupService binLookupService;

    @Mock
    private TransactionService transactionService;

    private TransactionRequest transactionRequest;
    private String requestId;
    private String correlationId;
    private String username;

    @BeforeEach
    void setUp() {
        transactionRequest = new TransactionRequest("585240", new BigDecimal("100.00"), "New York", "USA");
        requestId = UUID.randomUUID().toString();
        correlationId = "client-correlation-123";
        username = "testUser";
    }

    @Test
    void evaluateTransaction_ShouldReturnSuccessResponse() {
        BinResponse binResponse = new BinResponse();
        when(binLookupService.lookupBin(transactionRequest.getBin())).thenReturn(Optional.of(binResponse));

        TransactionRiskEvaluator.RiskAssessment assessment = new TransactionRiskEvaluator.RiskAssessment(
                50,
                java.util.List.of(new TransactionRiskEvaluator.RiskFactor("TEST", 50, "Test Risk"))
        );
        when(transactionRiskEvaluator.evaluate(binResponse, transactionRequest)).thenReturn(assessment);

        Transaction mockTransaction = new Transaction();
        mockTransaction.setRiskScore(50);
        mockTransaction.setRiskReason("Test Risk");
        when(transactionService.saveTransaction(any(), any(), any(BigDecimal.class), any(), any(), anyInt(), any()))
                .thenReturn(mockTransaction);

        Response response = transactionFacade.evaluateTransaction(
                transactionRequest,
                requestId,
                correlationId,
                username
        );

        assertEquals(200, response.getStatus());
        Map<String, Object> responseBody = (Map<String, Object>) response.getEntity();
        assertEquals(requestId, responseBody.get("requestId"));
        assertEquals(50, responseBody.get("riskScore"));
        assertEquals("Test Risk", responseBody.get("explanation"));

        verify(binLookupService).lookupBin(transactionRequest.getBin());
        verify(transactionRiskEvaluator).evaluate(binResponse, transactionRequest);
        verify(transactionService).saveTransaction(
                requestId,
                transactionRequest.getBin(),
                transactionRequest.getAmount(),
                transactionRequest.getLocation(),
                transactionRequest.getCountryCode(),
                50,
                "Test Risk"
        );
    }

    @Test
    void evaluateTransaction_ShouldReturnNotFound_WhenBinNotFound() {
        when(binLookupService.lookupBin(transactionRequest.getBin())).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> transactionFacade.evaluateTransaction(
                        transactionRequest,
                        requestId,
                        correlationId,
                        username
                ));

        assertEquals(404, exception.getStatus().getStatusCode());
        assertEquals("BIN_NOT_FOUND", exception.getCode());
        assertEquals(transactionRequest.getBin(), exception.getBin());

        verify(binLookupService).lookupBin(transactionRequest.getBin());
        verifyNoInteractions(transactionRiskEvaluator, transactionService);
    }

    @Test
    void evaluateTransaction_ShouldReturnInternalServerError_OnException() {
        when(binLookupService.lookupBin(transactionRequest.getBin())).thenThrow(new RuntimeException("Unexpected error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionFacade.evaluateTransaction(
                        transactionRequest,
                        requestId,
                        correlationId,
                        username
                ));
        assertEquals("Unexpected error", exception.getMessage());

        verify(binLookupService).lookupBin(transactionRequest.getBin());
        verifyNoInteractions(transactionRiskEvaluator, transactionService);
    }
}
