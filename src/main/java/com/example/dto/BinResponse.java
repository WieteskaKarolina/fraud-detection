package com.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RegisterForReflection
@Schema(description = "Response DTO for BIN lookup")
public class BinResponse {

    @JsonProperty("binNum")
    @Schema(description = "The BIN number (first 6-9 digits of the card number)", example = "585240")
    private String binNum;

    @JsonProperty("binLength")
    @Schema(description = "Length of the BIN number", example = "6")
    private int binLength;

    @JsonProperty("acceptanceBrand")
    @Schema(description = "The card brand (e.g., Visa, MasterCard, etc.)", example = "MasterCard")
    private String acceptanceBrand;

    @JsonProperty("ica")
    @Schema(description = "Interchange Company Account (ICA) number", example = "123456")
    private String ica;

    @JsonProperty("customerName")
    @Schema(description = "The name of the customer associated with the BIN", example = "Bank of Example")
    private String customerName;

    @JsonProperty("smartDataEnabled")
    @Schema(description = "Indicates if Smart Data is enabled", example = "true")
    private boolean smartDataEnabled;

    @JsonProperty("anonymousPrepaidIndicator")
    @Schema(description = "Indicates if the card is an anonymous prepaid card", example = "Yes")
    private String anonymousPrepaidIndicator;

    @JsonProperty("consumerType")
    @Schema(description = "Type of consumer associated with the BIN (e.g., Personal, Business)", example = "Personal")
    private String consumerType;

    @JsonProperty("localUse")
    @Schema(description = "Indicates if the card is restricted to local use", example = "false")
    private boolean localUse;

    @JsonProperty("nonReloadableIndicator")
    @Schema(description = "Indicates if the card is non-reloadable", example = "false")
    private boolean nonReloadableIndicator;

    @JsonProperty("authorizationOnly")
    @Schema(description = "Indicates if the card can only be used for authorization, not transactions", example = "false")
    private boolean authorizationOnly;

    @JsonProperty("gamblingBlockEnabled")
    @Schema(description = "Indicates if gambling transactions are blocked", example = "false")
    @Builder.Default
    private Boolean gamblingBlockEnabled = false;

    @JsonProperty("fundingSource")
    @Schema(description = "The funding source type (e.g., Credit, Debit, Prepaid)", example = "Credit")
    private String fundingSource;

    @JsonProperty("country")
    @Schema(description = "Country information related to the BIN")
    @Builder.Default
    private Country country = new Country();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Country details for the BIN response")
    public static class Country {
        @JsonProperty("code")
        @Schema(description = "Numeric country code (ISO 3166-1 numeric)", example = "840")
        private int code;

        @JsonProperty("alpha3")
        @Schema(description = "Alpha-3 country code (ISO 3166-1 alpha-3)", example = "USA")
        private String alpha3;

        @JsonProperty("name")
        @Schema(description = "Full country name", example = "United States")
        private String name;
    }
}
