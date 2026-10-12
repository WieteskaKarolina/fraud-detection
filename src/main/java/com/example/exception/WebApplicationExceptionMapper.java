package com.example.exception;

import com.example.dto.ApiErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Override
    public Response toResponse(WebApplicationException exception) {
        Response original = exception.getResponse();
        int status = original == null ? 500 : original.getStatus();
        String code = switch (status) {
            case 400 -> "INVALID_REQUEST";
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "NOT_FOUND";
            default -> status >= 500 ? "REQUEST_FAILED" : "HTTP_ERROR";
        };
        String message = switch (status) {
            case 400 -> "The request is invalid";
            case 401 -> "Authentication is required";
            case 403 -> "You are not allowed to perform this action";
            case 404 -> "The requested resource was not found";
            case 405 -> "The requested method is not allowed";
            case 406 -> "The requested response format is not available";
            case 409 -> "The request conflicts with the current resource state";
            case 415 -> "The request format is not supported";
            case 429 -> "Too many requests";
            default -> status >= 500
                    ? "The request could not be completed"
                    : "The request could not be processed";
        };
        return Response.status(status).type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse(code, message, null)).build();
    }
}
