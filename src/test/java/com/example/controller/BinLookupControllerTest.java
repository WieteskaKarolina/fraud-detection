package com.example.controller;

import com.example.dto.BinResponse;
import com.example.exception.ApiException;
import com.example.facade.BinLookupFacade;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BinLookupControllerTest {

    @InjectMocks
    BinLookupController binLookupController;

    @Mock
    BinLookupFacade binLookupFacade;

    @Mock
    BinResponse binResponse;

    @Test
    void getBinDetails_shouldReturnBinDetails_whenBinExists() {
        // Mockujemy zwrócenie obiektu BinResponse zamiast Response
        when(binLookupFacade.getBinDetails("123456", "test-request-id", "client-correlation"))
                .thenReturn(binResponse);

        Response response = binLookupController.getBinDetails("123456", "test-request-id", "client-correlation");

        assertEquals(200, response.getStatus());
        assertEquals(binResponse, response.getEntity());
        verify(binLookupFacade).getBinDetails("123456", "test-request-id", "client-correlation");
    }

    @Test
    void getBinDetails_shouldThrowApiException_whenBinNotExists() {
        // Fasada rzuca teraz ApiException, gdy BIN nie zostanie znaleziony
        when(binLookupFacade.getBinDetails("999999", "test-request-id", "client-correlation"))
                .thenThrow(new ApiException(Response.Status.NOT_FOUND, "BIN_NOT_FOUND", "BIN details not found", "999999"));

        // Oczekujemy wyjątku ApiException w teście jednostkowym kontrolera
        ApiException exception = assertThrows(
                ApiException.class,
                () -> binLookupController.getBinDetails("999999", "test-request-id", "client-correlation")
        );

        assertEquals(Response.Status.NOT_FOUND, exception.getStatus());
        assertEquals("BIN_NOT_FOUND", exception.getCode());
        verify(binLookupFacade).getBinDetails("999999", "test-request-id", "client-correlation");
    }
}