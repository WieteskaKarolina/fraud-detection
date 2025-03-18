package com.example.service;

import com.example.model.BinResponse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class BinLookupServiceIntegrationTest {

    @Inject
    BinLookupService binLookupService;

    private static final String VALID_BIN = "585240";
    private static final String INVALID_BIN = "000000";

    @Test
    void testLookupBinWithValidData() {
        Optional<BinResponse> resultOpt = binLookupService.lookupBin(VALID_BIN);

        assertTrue(resultOpt.isPresent(), "BinResponse should be present");
        BinResponse result = resultOpt.get();

        assertEquals("585240", result.getBinNum());
        assertEquals(6, result.getBinLength());
        assertEquals("Poland", result.getCountry().getName());
        assertEquals("CIR", result.getAcceptanceBrand());
    }

    @Test
    void testLookupBinWithNoData() {
        try {
        Optional<BinResponse> resultOpt = binLookupService.lookupBin(INVALID_BIN);

            assertTrue(resultOpt.isEmpty(), "BinResponse should be empty for a BIN with no data");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception occurred: " + e.getMessage());
        }
    }
}
