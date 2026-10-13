package com.example.filter;

import com.example.dto.ApiErrorResponse;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.MDC;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Provider
@PreMatching
@Priority(Priorities.HEADER_DECORATOR)
public class RequestIdFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String HEADER_NAME = "X-Request-Id";
    public static final String CORRELATION_HEADER_NAME = "X-Correlation-Id";
    static final String PROPERTY_NAME = RequestIdFilter.class.getName() + ".requestId";
    static final String CORRELATION_PROPERTY_NAME = RequestIdFilter.class.getName() + ".correlationId";
    private static final Pattern CORRELATION_ID_PATTERN = Pattern.compile("[A-Za-z0-9._:-]{1,128}");

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        MDC.remove("requestId");
        MDC.remove("correlationId");
        String requestId = UUID.randomUUID().toString();
        requestContext.getHeaders().putSingle(HEADER_NAME, requestId);
        requestContext.setProperty(PROPERTY_NAME, requestId);
        MDC.put("requestId", requestId);

        String correlationId = requestContext.getHeaderString(CORRELATION_HEADER_NAME);
        if (correlationId == null || correlationId.isBlank()) {
            requestContext.getHeaders().remove(CORRELATION_HEADER_NAME);
            return;
        }

        correlationId = correlationId.trim();
        if (!CORRELATION_ID_PATTERN.matcher(correlationId).matches()) {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ApiErrorResponse(
                            "INVALID_CORRELATION_ID",
                            "X-Correlation-Id must contain 1 to 128 letters, digits, or . _ : - characters",
                            null
                    ))
                    .build());
            return;
        }

        requestContext.getHeaders().putSingle(CORRELATION_HEADER_NAME, correlationId);
        requestContext.setProperty(CORRELATION_PROPERTY_NAME, correlationId);
        MDC.put("correlationId", correlationId);
    }

    @Override
    public void filter(
            ContainerRequestContext requestContext,
            ContainerResponseContext responseContext
    ) throws IOException {
        try {
            Object requestId = requestContext.getProperty(PROPERTY_NAME);
            if (requestId != null) {
                MultivaluedMap<String, Object> headers = responseContext.getHeaders();
                headers.putSingle(HEADER_NAME, requestId);

                Object correlationId = requestContext.getProperty(CORRELATION_PROPERTY_NAME);
                if (correlationId != null) {
                    headers.putSingle(CORRELATION_HEADER_NAME, correlationId);
                }
            }
        } finally {
            MDC.remove("requestId");
            MDC.remove("correlationId");
        }
    }
}
