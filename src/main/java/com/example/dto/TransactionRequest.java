package com.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for processing a transaction")
public class TransactionRequest {

    @NotNull
    @Size(min = 6, max = 11, message = "BIN must be between 6 and 11 digits")
    @Pattern(regexp = "\\d+", message = "BIN must contain only digits")
    @Schema(description = "Bank Identification Number (BIN), first 6-11 digits of the card number",
            example = "585240")
    private String bin;

    @NotNull
    @DecimalMin(value = "0.0", message = "Amount must be greater than or equal to zero")
    @Digits(integer = 17, fraction = 2, message = "Amount must have at most 17 integer digits and 2 decimal places")
    @Schema(description = "Transaction amount", example = "100.50")
    private BigDecimal amount;

    @NotNull
    @Schema(description = "Location where the transaction is being processed",
            example = "New York, USA")
    private String location;

    @NotBlank
    @Pattern(regexp = "[A-Za-z]{3}", message = "Country code must be an ISO 3166-1 alpha-3 code")
    @Schema(description = "ISO 3166-1 alpha-3 country code for the transaction location", example = "USA")
    private String countryCode;
}
