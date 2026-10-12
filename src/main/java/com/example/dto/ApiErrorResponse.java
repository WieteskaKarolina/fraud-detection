package com.example.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@RegisterForReflection
@Schema(description = "Standard API error response")
public class ApiErrorResponse {
    @Schema(example = "BIN_NOT_FOUND")
    public String code;
    @Schema(example = "BIN details not found")
    public String message;
    @Schema(example = "585240")
    public String bin;
    @Schema(description = "Validation errors, when the request has multiple invalid fields")
    public List<String> errors;

    public ApiErrorResponse() {}

    public ApiErrorResponse(String code, String message, String bin) {
        this.code = code;
        this.message = message;
        this.bin = bin;
    }

    public ApiErrorResponse(String code, String message, String bin, List<String> errors) {
        this(code, message, bin);
        this.errors = errors;
    }
}
