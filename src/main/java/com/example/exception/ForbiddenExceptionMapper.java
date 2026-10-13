package com.example.exception;

import com.example.dto.ApiErrorResponse;
import io.quarkus.security.ForbiddenException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {

    @Override
    public Response toResponse(ForbiddenException exception) {
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse(
                        "FORBIDDEN",
                        "You are not allowed to access this resource",
                        null))
                .build();
    }
}
