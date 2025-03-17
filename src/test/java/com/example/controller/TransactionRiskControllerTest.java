package com.example.controller;

import com.example.dto.TransactionRequest;
import com.example.model.BinResponse;
import com.example.service.BinLookupService;
import com.example.service.FraudDetectionService;
import com.example.service.TransactionService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionRiskControllerTest {

    @InjectMocks
    TransactionRiskController transactionRiskController;

    @Mock
    FraudDetectionService fraudDetectionService;

    @Mock
    BinLookupService binLookupService;

    @Mock
    TransactionService transactionService;

    @Mock
    JsonWebToken jwt;

    @Mock
    Logger logger;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void evaluateTransactionRisk_shouldReturnNotFound_whenBinNotFound() throws Exception {
        TransactionRequest request = new TransactionRequest("000000", 50.0, "LA");
        when(binLookupService.lookupBin("000000")).thenReturn(Optional.empty());

        Response response = transactionRiskController.evaluateTransactionRisk(request, null, null);

        assertEquals(404, response.getStatus());
        Map<String, Object> responseBody = (Map<String, Object>) response.getEntity();
        assertEquals("BIN details not found", responseBody.get("error"));
    }

    @Test
    void evaluateTransactionRisk_shouldHandleInternalServerError() throws Exception {
        TransactionRequest request = new TransactionRequest("123456", 100.0, "NY");
        when(binLookupService.lookupBin("123456")).thenThrow(new RuntimeException("Unexpected error"));

        Response response = transactionRiskController.evaluateTransactionRisk(request, null, null);

        assertEquals(500, response.getStatus());
        Map<String, Object> responseBody = (Map<String, Object>) response.getEntity();
        assertEquals("Error evaluating transaction risk", responseBody.get("error"));
    }

    @Test
    void getBinDetails_shouldReturnBinDetails_whenBinExists() throws Exception {
        BinResponse mockBinResponse = new BinResponse();
        when(binLookupService.lookupBin("123456")).thenReturn(Optional.of(mockBinResponse));

        Response response = transactionRiskController.getBinDetails("123456");

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
    }


    @Test
    void getBinDetails_shouldReturnNotFound_whenBinNotExists() throws Exception {
        when(binLookupService.lookupBin("999999")).thenReturn(Optional.empty());

        Response response = transactionRiskController.getBinDetails("999999");

        assertEquals(404, response.getStatus());
        Map<String, Object> responseBody = (Map<String, Object>) response.getEntity();
        assertEquals("BIN details not found", responseBody.get("error"));
    }
}
