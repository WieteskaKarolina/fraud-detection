package com.example.controller;

import com.example.dto.TransactionRequest;
import com.example.facade.TransactionFacade;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionRiskControllerTest {

    @InjectMocks
    TransactionRiskController transactionRiskController;

    @Mock
    TransactionFacade transactionFacade;

    @Mock
    JsonWebToken jwt;

    @Test
    void evaluateTransactionRisk_shouldReturnNotFound_whenBinNotFound() {
        TransactionRequest request = new TransactionRequest("000000", new BigDecimal("50.00"), "LA");
        when(jwt.getName()).thenReturn("test-user");
        when(transactionFacade.evaluateTransaction(any(), any(), any())).thenReturn(
                Response.status(404).entity(Map.of("error", "BIN details not found")).build()
        );

        Response response = transactionRiskController.evaluateTransactionRisk(request, null);

        assertEquals(404, response.getStatus());
        Map<?, ?> responseBody = (Map<?, ?>) response.getEntity();
        assertEquals("BIN details not found", responseBody.get("error"));
        verify(transactionFacade).evaluateTransaction(request, null, "test-user");
    }

    @Test
    void evaluateTransactionRisk_shouldHandleInternalServerError() {
        TransactionRequest request = new TransactionRequest("123456", new BigDecimal("100.00"), "NY");
        when(jwt.getName()).thenReturn("test-user");
        when(transactionFacade.evaluateTransaction(any(), any(), any())).thenReturn(
                Response.status(500).entity(Map.of("error", "Error evaluating transaction risk")).build()
        );

        Response response = transactionRiskController.evaluateTransactionRisk(request, null);

        assertEquals(500, response.getStatus());
        Map<?, ?> responseBody = (Map<?, ?>) response.getEntity();
        assertEquals("Error evaluating transaction risk", responseBody.get("error"));
        verify(transactionFacade).evaluateTransaction(request, null, "test-user");
    }
}
