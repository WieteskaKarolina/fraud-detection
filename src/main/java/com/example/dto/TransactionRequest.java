package com.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TransactionRequest {
    @NotNull
    @Size(min = 6, max = 11, message = "BIN must be between 6 and 11 digits")
    @Pattern(regexp = "\\d+", message = "BIN must contain only digits")
    public String bin;

    @Min(0)
    public double amount;

    @NotNull
    public String location;

    public TransactionRequest(String bin, double amount, String location) {
        this.bin = bin;
        this.amount = amount;
        this.location = location;
    }

    public TransactionRequest() {
    }

    public String getBin() {
        return bin;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }
}
