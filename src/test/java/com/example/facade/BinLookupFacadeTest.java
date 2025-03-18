package com.example.facade;

import com.example.dto.BinResponse;
import com.example.service.BinLookupService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BinLookupFacadeTest {

    @InjectMocks
    private BinLookupFacade binLookupFacade;

    @Mock
    private BinLookupService binLookupService;

    @Mock
    private Logger logger;

    private final String validBin = "585240";
    private final String invalidBin = "999999";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

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

        Response response = binLookupFacade.getBinDetails(invalidBin);

        assertEquals(404, response.getStatus());
        Map<String, Object> responseBody = (Map<String, Object>) response.getEntity();
        assertEquals("BIN details not found", responseBody.get("error"));
        assertEquals(invalidBin, responseBody.get("bin"));

        verify(binLookupService).lookupBin(invalidBin);
    }

    @Test
    void getBinDetails_ShouldReturnInternalServerError_OnException() {
        when(binLookupService.lookupBin(validBin)).thenThrow(new RuntimeException("Unexpected error"));

        Response response = binLookupFacade.getBinDetails(validBin);

        assertEquals(500, response.getStatus());
        Map<String, Object> responseBody = (Map<String, Object>) response.getEntity();
        assertEquals("Error fetching BIN details", responseBody.get("error"));
        assertEquals("Unexpected error", responseBody.get("message"));

        verify(binLookupService).lookupBin(validBin);
    }
}
