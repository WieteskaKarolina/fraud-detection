package com.example.model;

import org.json.JSONObject;

public class BinResponse {
    private String binNum;
    private int binLength;
    private String acceptanceBrand;
    private String ica;
    private String customerName;
    private boolean smartDataEnabled;
    private Country country;
    private String anonymousPrepaidIndicator;
    private String consumerType;
    private boolean localUse;
    private boolean nonReloadableIndicator;
    private boolean authorizationOnly;
    private boolean gamblingBlockEnabled;
    private String fundingSource;


    public String getAnonymousPrepaidIndicator() {
        return anonymousPrepaidIndicator;
    }

    public void setAnonymousPrepaidIndicator(String anonymousPrepaidIndicator) {
        this.anonymousPrepaidIndicator = anonymousPrepaidIndicator;
    }

    public String getConsumerType() {
        return consumerType;
    }

    public void setConsumerType(String consumerType) {
        this.consumerType = consumerType;
    }

    public boolean isLocalUse() {
        return localUse;
    }

    public void setLocalUse(boolean localUse) {
        this.localUse = localUse;
    }

    public boolean isNonReloadableIndicator() {
        return nonReloadableIndicator;
    }

    public void setNonReloadableIndicator(boolean nonReloadableIndicator) {
        this.nonReloadableIndicator = nonReloadableIndicator;
    }

    public boolean isAuthorizationOnly() {
        return authorizationOnly;
    }

    public void setAuthorizationOnly(boolean authorizationOnly) {
        this.authorizationOnly = authorizationOnly;
    }

    public boolean isGamblingBlockEnabled() {
        return gamblingBlockEnabled;
    }

    public void setGamblingBlockEnabled(boolean gamblingBlockEnabled) {
        this.gamblingBlockEnabled = gamblingBlockEnabled;
    }

    public String getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(String fundingSource) {
        this.fundingSource = fundingSource;
    }

    public static BinResponse fromJson(JSONObject json) {
        BinResponse binResponse = new BinResponse();
        binResponse.setBinNum(json.optString("binNum"));
        binResponse.setBinLength(json.optInt("binLength"));
        binResponse.setAcceptanceBrand(json.optString("acceptanceBrand"));
        binResponse.setIca(json.optString("ica"));
        binResponse.setCustomerName(json.optString("customerName"));
        binResponse.setSmartDataEnabled(json.optBoolean("smartDataEnabled"));
        binResponse.setAnonymousPrepaidIndicator(json.optString("anonymousPrepaidIndicator"));
        binResponse.setConsumerType(json.optString("consumerType"));
        binResponse.setLocalUse(json.optBoolean("localUse"));
        binResponse.setNonReloadableIndicator(json.optBoolean("nonReloadableIndicator"));
        binResponse.setAuthorizationOnly(json.optBoolean("authorizationOnly"));
        binResponse.setGamblingBlockEnabled(json.optBoolean("gamblingBlockEnabled"));
        binResponse.setFundingSource(json.optString("fundingSource"));

        JSONObject countryJson = json.optJSONObject("country");
        if (countryJson != null) {
            Country country = new Country();
            country.setCode(countryJson.optInt("code"));
            country.setAlpha3(countryJson.optString("alpha3"));
            country.setName(countryJson.optString("name"));
            binResponse.setCountry(country);
        }

        return binResponse;
    }

    public String getBinNum() {
        return binNum;
    }

    public void setBinNum(String binNum) {
        this.binNum = binNum;
    }

    public int getBinLength() {
        return binLength;
    }

    public void setBinLength(int binLength) {
        this.binLength = binLength;
    }

    public String getAcceptanceBrand() {
        return acceptanceBrand;
    }

    public void setAcceptanceBrand(String acceptanceBrand) {
        this.acceptanceBrand = acceptanceBrand;
    }

    public String getIca() {
        return ica;
    }

    public void setIca(String ica) {
        this.ica = ica;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public boolean isSmartDataEnabled() {
        return smartDataEnabled;
    }

    public void setSmartDataEnabled(boolean smartDataEnabled) {
        this.smartDataEnabled = smartDataEnabled;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public static class Country {
        private int code;
        private String alpha3;
        private String name;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getAlpha3() {
            return alpha3;
        }

        public void setAlpha3(String alpha3) {
            this.alpha3 = alpha3;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
