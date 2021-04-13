/**
 * Copyright (c) 2020 Bosch.IO GmbH, Germany. All rights reserved.
 */

package io.bosch.measurement.ditto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Things client specific configuration properties
 */
@Configuration
@ConfigurationProperties(prefix = "authentication")
@Data
public class AuthenticationProperties {

    private String oauthTokenEndpoint;
    private String wssThingsEndpointUrl;
    private String clientId;
    private String clientSecret;
    private String serviceInstanceId;
    private String deviceId;
}
