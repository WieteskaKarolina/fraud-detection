package com.example.filter;

import com.example.dto.ApiErrorResponse;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import org.jboss.logging.MDC;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RequestIdFilterTest {

    private final RequestIdFilter requestIdFilter = new RequestIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.remove("requestId");
        MDC.remove("correlationId");
    }

    @Test
    void filter_shouldGenerateUuidAndReplaceClientProvidedRequestId() throws Exception {
        ContainerRequestContext requestContext = mockRequestContext("client-request-123", null);

        requestIdFilter.filter(requestContext);

        String requestId = requestContext.getHeaders().getFirst(RequestIdFilter.HEADER_NAME);
        assertNotNull(requestId);
        assertDoesNotThrow(() -> UUID.fromString(requestId));
        assertNotEquals("client-request-123", requestId);
        verify(requestContext).setProperty(RequestIdFilter.PROPERTY_NAME, requestId);
        assertEquals(requestId, MDC.get("requestId"));
    }

    @Test
    void filter_shouldGenerateUuidWhenRequestIdIsMissing() throws Exception {
        ContainerRequestContext requestContext = mockRequestContext(null, null);

        requestIdFilter.filter(requestContext);

        String requestId = requestContext.getHeaders().getFirst(RequestIdFilter.HEADER_NAME);
        assertNotNull(requestId);
        assertDoesNotThrow(() -> UUID.fromString(requestId));
        verify(requestContext).setProperty(RequestIdFilter.PROPERTY_NAME, requestId);
    }

    @Test
    void responseFilter_shouldReturnSameRequestIdInResponseHeader() throws Exception {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
        MultivaluedMap<String, Object> responseHeaders = new MultivaluedHashMap<>();
        when(requestContext.getProperty(RequestIdFilter.PROPERTY_NAME)).thenReturn("request-123");
        when(responseContext.getHeaders()).thenReturn(responseHeaders);

        requestIdFilter.filter(requestContext, responseContext);

        assertEquals("request-123", responseHeaders.getFirst(RequestIdFilter.HEADER_NAME));
    }

    @Test
    void filter_shouldPreserveValidClientCorrelationId() throws Exception {
        ContainerRequestContext requestContext = mockRequestContext(null, "  client:order-123  ");

        requestIdFilter.filter(requestContext);

        assertEquals(
                "client:order-123",
                requestContext.getHeaders().getFirst(RequestIdFilter.CORRELATION_HEADER_NAME)
        );
        verify(requestContext).setProperty(RequestIdFilter.CORRELATION_PROPERTY_NAME, "client:order-123");
        assertEquals("client:order-123", MDC.get("correlationId"));
    }

    @Test
    void filter_shouldRejectInvalidClientCorrelationId() throws Exception {
        ContainerRequestContext requestContext = mockRequestContext(null, "client id with spaces");

        requestIdFilter.filter(requestContext);

        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(responseCaptor.capture());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), responseCaptor.getValue().getStatus());
        assertEquals(
                "INVALID_CORRELATION_ID",
                ((ApiErrorResponse) responseCaptor.getValue().getEntity()).code
        );
    }

    @Test
    void responseFilter_shouldReturnClientCorrelationIdWhenProvided() throws Exception {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
        MultivaluedMap<String, Object> responseHeaders = new MultivaluedHashMap<>();
        when(requestContext.getProperty(RequestIdFilter.PROPERTY_NAME)).thenReturn("request-123");
        when(requestContext.getProperty(RequestIdFilter.CORRELATION_PROPERTY_NAME)).thenReturn("client-123");
        when(responseContext.getHeaders()).thenReturn(responseHeaders);

        requestIdFilter.filter(requestContext, responseContext);

        assertEquals("request-123", responseHeaders.getFirst(RequestIdFilter.HEADER_NAME));
        assertEquals("client-123", responseHeaders.getFirst(RequestIdFilter.CORRELATION_HEADER_NAME));
        org.junit.jupiter.api.Assertions.assertNull(MDC.get("requestId"));
        org.junit.jupiter.api.Assertions.assertNull(MDC.get("correlationId"));
    }

    private ContainerRequestContext mockRequestContext(String requestId, String correlationId) {
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        MultivaluedMap<String, String> requestHeaders = new MultivaluedHashMap<>();
        if (requestId != null) {
            requestHeaders.putSingle(RequestIdFilter.HEADER_NAME, requestId);
        }
        if (correlationId != null) {
            requestHeaders.putSingle(RequestIdFilter.CORRELATION_HEADER_NAME, correlationId);
        }
        when(requestContext.getHeaderString(RequestIdFilter.CORRELATION_HEADER_NAME)).thenReturn(correlationId);
        when(requestContext.getHeaders()).thenReturn(requestHeaders);
        return requestContext;
    }
}
