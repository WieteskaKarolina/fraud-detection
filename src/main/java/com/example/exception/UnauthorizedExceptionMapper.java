package com.example.exception;

import com.example.dto.ApiErrorResponse;
import io.quarkus.security.UnauthorizedException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException> {

    @Override
    public Response toResponse(UnauthorizedException exception) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .header("WWW-Authenticate", "Bearer")
                .entity(new ApiErrorResponse("UNAUTHORIZED", "Authentication is required", null))
                .build();
    }
}
