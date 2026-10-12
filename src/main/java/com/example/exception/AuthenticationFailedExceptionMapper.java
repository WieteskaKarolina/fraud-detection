package com.example.exception;

import com.example.dto.ApiErrorResponse;
import io.quarkus.security.AuthenticationFailedException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFailedExceptionMapper
        implements ExceptionMapper<AuthenticationFailedException> {

    @Override
    public Response toResponse(AuthenticationFailedException exception) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .header("WWW-Authenticate", "Bearer")
                .entity(new ApiErrorResponse("UNAUTHORIZED", "Authentication is required", null))
                .build();
    }
}
