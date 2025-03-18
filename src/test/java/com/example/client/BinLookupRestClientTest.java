package com.example.client;

import com.example.dto.BinRequest;
import com.example.dto.BinResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BinLookupRestClientTest {

    @Mock
    @RestClient
    private BinLookupRestClient binLookupRestClient;

    private final BinRequest mockRequest = new BinRequest("585240");
    private final BinResponse mockResponse = new BinResponse();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void lookupBin_ShouldReturnBinResponseList() {
        when(binLookupRestClient.lookupBin(mockRequest)).thenReturn(Collections.singletonList(mockResponse));

        List<BinResponse> response = binLookupRestClient.lookupBin(mockRequest);

        assertNotNull(response);
        assertFalse(response.isEmpty());

        verify(binLookupRestClient, times(1)).lookupBin(mockRequest);
    }

    @Test
    void lookupBin_ShouldReturnEmptyList_WhenNoData() {
        when(binLookupRestClient.lookupBin(mockRequest)).thenReturn(Collections.emptyList());

        List<BinResponse> response = binLookupRestClient.lookupBin(mockRequest);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }
}
