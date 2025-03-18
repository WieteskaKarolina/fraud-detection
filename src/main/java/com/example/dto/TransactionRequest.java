package com.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

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

    @Min(0)
    @Schema(description = "Transaction amount", example = "100.50")
    private double amount;

    @NotNull
    @Schema(description = "Location where the transaction is being processed",
            example = "New York, USA")
    private String location;
}
