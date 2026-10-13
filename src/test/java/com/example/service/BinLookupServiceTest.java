package com.example.service;

import com.example.client.BinLookupRestClient;
import com.example.dto.BinRequest;
import com.example.dto.BinResponse;
import com.example.exception.ApiException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BinLookupServiceTest {

    private static final String TEST_BIN = "585240";

    @Mock
    BinLookupRestClient binLookupRestClient;

    @InjectMocks
    BinLookupService binLookupService;

    @Test
    void returnsFirstBinWhenProviderFindsMatch() {
        BinResponse expected = createBinResponse();
        when(binLookupRestClient.lookupBin(any())).thenReturn(List.of(expected));

        Optional<BinResponse> result = binLookupService.lookupBin(TEST_BIN);

        assertTrue(result.isPresent());
        assertSame(expected, result.orElseThrow());

        ArgumentCaptor<BinRequest> requestCaptor =
                ArgumentCaptor.forClass(BinRequest.class);
        verify(binLookupRestClient).lookupBin(requestCaptor.capture());
        assertEquals(TEST_BIN, requestCaptor.getValue().getAccountRange());
    }

    @Test
    void returnsEmptyWhenProviderReturnsNoMatches() {
        when(binLookupRestClient.lookupBin(any()))
                .thenReturn(Collections.emptyList());

        assertTrue(binLookupService.lookupBin(TEST_BIN).isEmpty());
    }

    @Test
    void mapsProvider404ToIntegrationError() {
        mockHttpError(404);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> binLookupService.lookupBin(TEST_BIN)
        );

        assertAll(
                () -> assertEquals("MASTERCARD_INTEGRATION_ERROR", exception.getCode()),
                () -> assertEquals(502, exception.getStatus().getStatusCode()),
                () -> assertEquals(TEST_BIN, exception.getBin())
        );
    }

    @ParameterizedTest
    @CsvSource({
            "400, MASTERCARD_INTEGRATION_ERROR, 502",
            "401, MASTERCARD_AUTHENTICATION_FAILED, 502",
            "403, MASTERCARD_PERMISSION_DENIED, 502",
            "429, MASTERCARD_UNAVAILABLE, 503",
            "500, MASTERCARD_UNAVAILABLE, 503",
            "503, MASTERCARD_INTEGRATION_ERROR, 502"
    })
    void mapsProviderHttpErrors(int providerStatus, String expectedCode, int expectedStatus) {
        mockHttpError(providerStatus);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> binLookupService.lookupBin(TEST_BIN)
        );

        assertAll(
                () -> assertEquals(expectedCode, exception.getCode()),
                () -> assertEquals(expectedStatus, exception.getStatus().getStatusCode()),
                () -> assertEquals(TEST_BIN, exception.getBin())
        );
    }

    @Test
    void mapsTimeoutToGatewayTimeout() {
        when(binLookupRestClient.lookupBin(any()))
                .thenThrow(new ProcessingException(
                        "Client call failed",
                        new SocketTimeoutException("Timed out")
                ));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> binLookupService.lookupBin(TEST_BIN)
        );

        assertAll(
                () -> assertEquals("MASTERCARD_TIMEOUT", exception.getCode()),
                () -> assertEquals(504, exception.getStatus().getStatusCode()),
                () -> assertEquals(TEST_BIN, exception.getBin())
        );
    }

    private void mockHttpError(int status) {
        when(binLookupRestClient.lookupBin(any()))
                .thenThrow(new WebApplicationException(Response.status(status).build()));
    }

    private static BinResponse createBinResponse() {
        BinResponse response = new BinResponse();
        response.setBinNum(TEST_BIN);
        response.setBinLength(6);
        response.setAcceptanceBrand("CIR");
        response.setCustomerName("KOHO FINANCIAL INCORPORATED");
        response.setCountry(new BinResponse.Country(616, "POL", "Poland"));
        return response;
    }
}
