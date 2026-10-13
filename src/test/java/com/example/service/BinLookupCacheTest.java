package com.example.service;

import com.example.client.BinLookupRestClient;
import com.example.dto.BinResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class BinLookupCacheTest {

    private static final String BIN = "991234";

    @Inject
    BinLookupService binLookupService;

    @InjectMock
    @RestClient
    BinLookupRestClient binLookupRestClient;

    @Test
    void repeatedLookupOfSameBinUsesCachedResponse() {
        BinResponse response = new BinResponse();
        response.setBinNum(BIN);
        when(binLookupRestClient.lookupBin(any())).thenReturn(List.of(response));

        Optional<BinResponse> firstLookup = binLookupService.lookupBin(BIN);
        Optional<BinResponse> secondLookup = binLookupService.lookupBin(BIN);

        assertTrue(firstLookup.isPresent());
        assertSame(response, firstLookup.orElseThrow());
        assertSame(response, secondLookup.orElseThrow());
        verify(binLookupRestClient, times(1)).lookupBin(any());
    }
}
