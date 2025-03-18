package com.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull
    @Size(min = 6, max = 11, message = "BIN must be between 6 and 11 digits")
    @Pattern(regexp = "\\d+", message = "BIN must contain only digits")
    private String bin;

    @Min(0)
    private double amount;

    @NotNull
    private String location;
}
