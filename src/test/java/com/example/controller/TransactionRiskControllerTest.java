package com.example.controller;

import com.example.dto.TransactionRequest;
import com.example.dto.RiskFactor;
import com.example.dto.TransactionRiskResponse;
import com.example.exception.ApiException;
import com.example.facade.TransactionFacade;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

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
    void evaluateTransactionRisk_shouldReturnTypedSuccessResponse() {
        TransactionRequest request = new TransactionRequest("585240", new BigDecimal("50.00"), "LA", "USA");
        when(jwt.getName()).thenReturn("test-user");
        TransactionRiskResponse expected = new TransactionRiskResponse(
                "generated-request-id",
                25,
                "Country mismatch",
                List.of(new RiskFactor("ISSUER_COUNTRY_MISMATCH", 25, "Country mismatch"))
        );
        when(transactionFacade.evaluateTransaction(any(), any(), any(), any())).thenReturn(expected);

        Response response = transactionRiskController.evaluateTransactionRisk(
                request,
                "generated-request-id",
                "client-correlation"
        );

        assertEquals(200, response.getStatus());
        assertEquals(expected, response.getEntity());
        verify(transactionFacade).evaluateTransaction(
                request,
                "generated-request-id",
                "client-correlation",
                "test-user"
        );
    }

    @Test
    void evaluateTransactionRisk_shouldPropagateApiException() {
        TransactionRequest request = new TransactionRequest("000000", new BigDecimal("50.00"), "LA", "USA");
        when(jwt.getName()).thenReturn("test-user");
        ApiException expected = new ApiException(
                Response.Status.NOT_FOUND,
                "BIN_NOT_FOUND",
                "BIN details not found",
                request.getBin()
        );
        when(transactionFacade.evaluateTransaction(any(), any(), any(), any())).thenThrow(expected);

        ApiException actual = org.junit.jupiter.api.Assertions.assertThrows(
                ApiException.class,
                () -> transactionRiskController.evaluateTransactionRisk(
                        request,
                        "generated-request-id",
                        "client-correlation"
                )
        );

        assertEquals(expected, actual);
        verify(transactionFacade).evaluateTransaction(
                request,
                "generated-request-id",
                "client-correlation",
                "test-user"
        );
    }
}
