package com.example.service;

import com.example.model.BinResponse;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class BinLookupServiceTest {

    private BinLookupService binLookupService;
    private OkHttpClient mockClient;
    private Call mockCall;

    private static final String BIN_API_URL = "https://sandbox.api.mastercard.com/bin-resources/bin-ranges/account-searches";
    private static final String MOCK_JSON_RESPONSE = "[{\"lowAccountRange\":5852400000000000000," +
            "\"highAccountRange\":5852409999999999999," +
            "\"binNum\":\"585240\"," +
            "\"binLength\":6," +
            "\"acceptanceBrand\":\"CIR\"," +
            "\"ica\":\"19668\"," +
            "\"customerName\":\"KOHO FInullCIAL INCORPORATED\"," +
            "\"smartDataEnabled\":false," +
            "\"country\":{\"code\":616,\"alpha3\":\"POL\",\"name\":\"Poland\"}," +
            "\"localUse\":false," +
            "\"authorizationOnly\":false," +
            "\"productCode\":\"MCS\"," +
            "\"productDescription\":\"STANDARD\"," +
            "\"governmentRange\":false," +
            "\"nonReloadableIndicator\":false," +
            "\"anonymousPrepaidIndicator\":\"N\"," +
            "\"cardholderCurrencyIndicator\":\"D\"," +
            "\"billingCurrencyDefault\":\"XXX\"," +
            "\"comboCardIndicator\":\"N\"," +
            "\"flexCardIndicator\":\"N\"," +
            "\"gamblingBlockEnabled\":null," +
            "\"programName\":\"UNAVAILABLE\"," +
            "\"vertical\":\"UNAVAILABLE\"," +
            "\"fundingSource\":\"NONE\"," +
            "\"consumerType\":\"CORPORATE\"," +
            "\"affiliate\":\"UNAVAILABLE\"," +
            "\"paymentAccountType\":\"P\"," +
            "\"credentialStatus\":\"A\"}]";

    @BeforeEach
    void setUp() {
        mockClient = mock(OkHttpClient.class);
        mockCall = mock(Call.class);

        binLookupService = new BinLookupService(mockClient);
        binLookupService.setBinApiUrl(BIN_API_URL);
    }

    @Test
    void testLookupBinSuccess() throws Exception {
        Response mockResponse = mock(Response.class);
        when(mockResponse.body()).thenReturn(ResponseBody.create(MediaType.get("application/json"), MOCK_JSON_RESPONSE));
        when(mockResponse.isSuccessful()).thenReturn(true);

        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        Optional<BinResponse> resultOpt = binLookupService.lookupBin("585240");

        assertTrue(resultOpt.isPresent(), "BinResponse should be present");

        BinResponse result = resultOpt.get();
        assertEquals("585240", result.getBinNum());
        assertEquals(6, result.getBinLength());
        assertEquals("Poland", result.getCountry().getName());
        assertEquals("KOHO FInullCIAL INCORPORATED", result.getCustomerName());
        assertEquals("CIR", result.getAcceptanceBrand());
    }

    @Test
    void testLookupBinNoContent() throws Exception {
        Response mockResponse = mock(Response.class);
        when(mockResponse.body()).thenReturn(null);
        when(mockResponse.isSuccessful()).thenReturn(true);

        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        Optional<BinResponse> resultOpt = binLookupService.lookupBin("123456");

        assertTrue(resultOpt.isEmpty(), "BinResponse should be empty when no content is returned");
    }

    @Test
    void testLookupBinFailure() throws Exception {
        Response mockResponse = mock(Response.class);
        when(mockResponse.isSuccessful()).thenReturn(false);

        when(mockClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);

        assertThrows(IOException.class, () -> binLookupService.lookupBin("585240"));
    }
}
