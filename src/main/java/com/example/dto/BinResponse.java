package com.example.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BinResponse {

    @JsonProperty("binNum")
    private String binNum;

    @JsonProperty("binLength")
    private int binLength;

    @JsonProperty("acceptanceBrand")
    private String acceptanceBrand;

    @JsonProperty("ica")
    private String ica;

    @JsonProperty("customerName")
    private String customerName;

    @JsonProperty("smartDataEnabled")
    private boolean smartDataEnabled;

    @JsonProperty("anonymousPrepaidIndicator")
    private String anonymousPrepaidIndicator;

    @JsonProperty("consumerType")
    private String consumerType;

    @JsonProperty("localUse")
    private boolean localUse;

    @JsonProperty("nonReloadableIndicator")
    private boolean nonReloadableIndicator;

    @JsonProperty("authorizationOnly")
    private boolean authorizationOnly;

    @JsonProperty("gamblingBlockEnabled")
    @Builder.Default
    private Boolean gamblingBlockEnabled = false;

    @JsonProperty("fundingSource")
    private String fundingSource;

    @JsonProperty("country")
    @Builder.Default
    private Country country = new Country();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Country {
        @JsonProperty("code")
        private int code;

        @JsonProperty("alpha3")
        private String alpha3;

        @JsonProperty("name")
        private String name;
    }
}
