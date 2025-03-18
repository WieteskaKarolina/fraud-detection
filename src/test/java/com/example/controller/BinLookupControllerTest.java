package com.example.controller;

import com.example.facade.BinLookupFacade;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BinLookupControllerTest {

    @InjectMocks
    BinLookupController binLookupController;

    @Mock
    BinLookupFacade binLookupFacade;

    @Mock
    Logger logger;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getBinDetails_shouldReturnBinDetails_whenBinExists() {
        when(binLookupFacade.getBinDetails("123456")).thenReturn(
                Response.ok(Map.of("bin", "123456", "status", "valid")).build()
        );

        Response response = binLookupController.getBinDetails("123456");

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
    }

    @Test
    void getBinDetails_shouldReturnNotFound_whenBinNotExists() {
        when(binLookupFacade.getBinDetails("999999")).thenReturn(
                Response.status(404).entity(Map.of("error", "BIN details not found")).build()
        );

        Response response = binLookupController.getBinDetails("999999");

        assertEquals(404, response.getStatus());
        Map<String, Object> responseBody = (Map<String, Object>) response.getEntity();
        assertEquals("BIN details not found", responseBody.get("error"));
    }
}
