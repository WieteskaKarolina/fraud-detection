package com.example.auth;

import com.mastercard.developer.oauth.OAuth;
import com.mastercard.developer.utils.AuthenticationUtils;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MastercardAuthFilterTest {

    @Mock
    PrivateKey mockPrivateKey;

    @Mock
    ClientRequestContext requestContext;

    @Spy
    MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

    MastercardAuthFilter mastercardAuthFilter;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        try (MockedStatic<AuthenticationUtils> mockedAuthUtils = mockStatic(AuthenticationUtils.class)) {
            mockedAuthUtils.when(() -> AuthenticationUtils.loadSigningKey(anyString(), anyString(), anyString()))
                    .thenReturn(mockPrivateKey);

            mastercardAuthFilter = new MastercardAuthFilter(
                    "test/path/to/key.p12", "test-alias", "test-password", "test-consumer-key"
            );
        }

        lenient().when(requestContext.getHeaders()).thenReturn(headers);

    }

    @Test
    void filter_ShouldAddOAuthHeader() throws Exception {
        when(requestContext.getUri()).thenReturn(new URI("https://sandbox.api.mastercard.com/"));
        when(requestContext.getMethod()).thenReturn("POST");

        try (MockedStatic<OAuth> mockedOAuth = mockStatic(OAuth.class)) {
            mockedOAuth.when(() -> OAuth.getAuthorizationHeader(
                    any(URI.class), anyString(), anyString(), eq(StandardCharsets.UTF_8), anyString(), any(PrivateKey.class)
            )).thenReturn("OAuth fake-auth-header");

            mastercardAuthFilter.filter(requestContext);

            assertEquals(Collections.singletonList("OAuth fake-auth-header"), headers.get(HttpHeaders.AUTHORIZATION));
        }
    }

    @Test
    void filter_ShouldHandleOAuthException() throws Exception {

        when(requestContext.getUri()).thenReturn(new URI("https://sandbox.api.mastercard.com/"));
        when(requestContext.getMethod()).thenReturn("POST");

        try (MockedStatic<OAuth> mockedOAuth = mockStatic(OAuth.class)) {
            mockedOAuth.when(() -> OAuth.getAuthorizationHeader(any(), any(), any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("OAuth Error"));

            assertThrows(java.io.IOException.class, () -> mastercardAuthFilter.filter(requestContext));
        }
    }
}
