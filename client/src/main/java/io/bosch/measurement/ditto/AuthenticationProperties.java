/**
 * Copyright (c) 2020 Bosch.IO GmbH, Germany. All rights reserved.
 */

package io.bosch.measurement.ditto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Things client specific configuration properties
 */
@Configuration
@ConfigurationProperties(prefix = "authentication")
public class AuthenticationProperties {

    private String oauthTokenEndpoint;
    private String wssThingsEndpointUrl;
    private String clientId;
    private String clientSecret;
    private String serviceInstanceId;
    private String deviceId;

    public String getOauthTokenEndpoint() {
        return oauthTokenEndpoint;
    }

    public void setOauthTokenEndpoint(final String oauthTokenEndpoint) {
        this.oauthTokenEndpoint = oauthTokenEndpoint;
    }

    public String getWssThingsEndpointUrl() {
        return wssThingsEndpointUrl;
    }

    public void setWssThingsEndpointUrl(final String wssThingsEndpointUrl) {
        this.wssThingsEndpointUrl = wssThingsEndpointUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(final String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(final String deviceId) {
        this.deviceId = deviceId;
    }

}
