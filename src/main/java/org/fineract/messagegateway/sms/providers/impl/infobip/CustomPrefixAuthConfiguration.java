package org.fineract.messagegateway.sms.providers.impl.infobip;

import infobip.api.config.Configuration;

/**
 * Configuration for Infobip SMS provider with custom prefix authentication.
 */
public class CustomPrefixAuthConfiguration extends Configuration {

    private final String apiKey;
    private final String customPrefix;

    public CustomPrefixAuthConfiguration(String baseUrl, String apiKey, String customPrefix) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.customPrefix = customPrefix;
    }

    public CustomPrefixAuthConfiguration(String apiKey, String customPrefix) {
        this.baseUrl = "https://api.infobip.com";
        this.apiKey = apiKey;
        this.customPrefix = customPrefix;
    }

    @Override public String getAuthorizationHeader() {
        return this.customPrefix.concat(" ").concat( this.apiKey);
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getCustomPrefix() {
        return customPrefix;
    }
}
