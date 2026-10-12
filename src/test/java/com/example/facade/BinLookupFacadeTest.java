package com.example.facade;

import com.example.dto.BinResponse;
import com.example.exception.ApiException;
import com.example.service.BinLookupService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BinLookupFacadeTest {

    @InjectMocks
    private BinLookupFacade binLookupFacade;

    @Mock
    private BinLookupService binLookupService;

    private final String validBin = "585240";
    private final String invalidBin = "999999";

    @Test
    void getBinDetails_ShouldReturnSuccessResponse_WhenBinExists() {
        BinResponse mockBinResponse = new BinResponse();
        when(binLookupService.lookupBin(validBin)).thenReturn(Optional.of(mockBinResponse));

        Response response = binLookupFacade.getBinDetails(validBin);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertTrue(response.getEntity() instanceof BinResponse);

        verify(binLookupService).lookupBin(validBin);
    }

    @Test
    void getBinDetails_ShouldReturnNotFound_WhenBinNotExists() {
        when(binLookupService.lookupBin(invalidBin)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> binLookupFacade.getBinDetails(invalidBin));

        assertEquals(404, exception.getStatus().getStatusCode());
        assertEquals("BIN_NOT_FOUND", exception.getCode());
        assertEquals(invalidBin, exception.getBin());

        verify(binLookupService).lookupBin(invalidBin);
    }

    @Test
    void getBinDetails_ShouldReturnInternalServerError_OnException() {
        when(binLookupService.lookupBin(validBin)).thenThrow(new RuntimeException("Unexpected error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> binLookupFacade.getBinDetails(validBin));
        assertEquals("Unexpected error", exception.getMessage());

        verify(binLookupService).lookupBin(validBin);
    }
}
