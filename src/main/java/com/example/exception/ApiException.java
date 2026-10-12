package com.example.exception;

import jakarta.ws.rs.core.Response;

public class ApiException extends RuntimeException {
    private final Response.Status status;
    private final String code;
    private final String bin;

    public ApiException(Response.Status status, String code, String message, String bin) {
        super(message);
        this.status = status;
        this.code = code;
        this.bin = bin;
    }

    public Response.Status getStatus() { return status; }
    public String getCode() { return code; }
    public String getBin() { return bin; }
}
