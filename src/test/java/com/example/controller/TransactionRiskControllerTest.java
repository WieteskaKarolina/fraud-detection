package com.example.controller;

import com.example.dto.TransactionRequest;
import com.example.facade.TransactionFacade;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Mock
    Logger logger;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void evaluateTransactionRisk_shouldReturnNotFound_whenBinNotFound() {
        TransactionRequest request = new TransactionRequest("000000", 50.0, "LA");
        when(transactionFacade.evaluateTransaction(any(), any(), any())).thenReturn(
                Response.status(404).entity(Map.of("error", "BIN details not found")).build()
        );

        Response response = transactionRiskController.evaluateTransactionRisk(request, null, null);

        assertEquals(404, response.getStatus());
        Map<String, Object> responseBody = (Map<String, Object>) response.getEntity();
        assertEquals("BIN details not found", responseBody.get("error"));
    }

    @Test
    void evaluateTransactionRisk_shouldHandleInternalServerError() {
        TransactionRequest request = new TransactionRequest("123456", 100.0, "NY");
        when(transactionFacade.evaluateTransaction(any(), any(), any())).thenReturn(
                Response.status(500).entity(Map.of("error", "Error evaluating transaction risk")).build()
        );

        Response response = transactionRiskController.evaluateTransactionRisk(request, null, null);

        assertEquals(500, response.getStatus());
        Map<String, Object> responseBody = (Map<String, Object>) response.getEntity();
        assertEquals("Error evaluating transaction risk", responseBody.get("error"));
    }
}
