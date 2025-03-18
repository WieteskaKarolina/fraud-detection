package com.example.service;

import com.example.client.BinLookupRestClient;
import com.example.dto.BinResponse;
import io.quarkus.test.junit.QuarkusTest;;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@ExtendWith(MockitoExtension.class)
class BinLookupServiceTest {

    @Mock
    BinLookupRestClient binLookupRestClient;

    @InjectMocks
    BinLookupService binLookupService;

    private static final String TEST_BIN = "585240";
    private static final BinResponse MOCK_RESPONSE = createMockBinResponse();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLookupBinSuccess() {
        when(binLookupRestClient.lookupBin(any())).thenReturn(Collections.singletonList(MOCK_RESPONSE));

        Optional<BinResponse> resultOpt = binLookupService.lookupBin(TEST_BIN);

        assertTrue(resultOpt.isPresent(), "BinResponse should be present");
        BinResponse result = resultOpt.get();
        assertEquals(TEST_BIN, result.getBinNum());
        assertEquals(6, result.getBinLength());
        assertEquals("Poland", result.getCountry().getName());
        assertEquals("CIR", result.getAcceptanceBrand());
    }

    @Test
    void testLookupBinNoContent() {
        when(binLookupRestClient.lookupBin(any())).thenReturn(Collections.emptyList());

        Optional<BinResponse> resultOpt = binLookupService.lookupBin(TEST_BIN);

        assertTrue(resultOpt.isEmpty(), "BinResponse should be empty when no content is returned");
    }

    @Test
    void testLookupBinFailure() {
        when(binLookupRestClient.lookupBin(any())).thenThrow(new RuntimeException("API error"));

        Optional<BinResponse> resultOpt = binLookupService.lookupBin(TEST_BIN);

        assertTrue(resultOpt.isEmpty(), "BinResponse should be empty in case of API failure");
    }

    private static BinResponse createMockBinResponse() {
        BinResponse response = new BinResponse();
        response.setBinNum(TEST_BIN);
        response.setBinLength(6);
        response.setAcceptanceBrand("CIR");
        response.setCustomerName("KOHO FINANCIAL INCORPORATED");
        response.setCountry(new BinResponse.Country(616, "POL", "Poland"));
        return response;
    }
}
