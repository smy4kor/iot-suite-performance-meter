/**
 * Copyright (c) 2020 Bosch.IO GmbH, Germany. All rights reserved.
 */

package io.bosch.measurement.ditto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.DittoClients;
import org.eclipse.ditto.client.configuration.ClientCredentialsAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.configuration.WebSocketMessagingConfiguration;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.AuthenticationProviders;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.messaging.MessagingProviders;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.neovisionaries.ws.client.WebSocket;

/**
 * Ditto client config to configure proxy and provide clients for live and twin
 * usage
 */
@Configuration
public class DittoClientConfig {

    public static final String LIVE_CLIENT = "liveClient";
    public static final String TWIN_CLIENT = "twinClient";

    private final AuthenticationProperties authenticationProperties;

    DittoClientConfig(final AuthenticationProperties authenticationProperties) {
        this.authenticationProperties = authenticationProperties;
    }

//    @Bean(LIVE_CLIENT)
//    DittoClient createLiveThingsClient() {
//        return DittoClients.newInstance(createDittoConfiguration());
//    }

    @Bean(TWIN_CLIENT)
    DittoClient createTwinThingsClient() {
        return DittoClients.newInstance(createDittoConfiguration());
    }

    private MessagingProvider createDittoConfiguration() {
        final MessagingConfiguration.Builder builder = WebSocketMessagingConfiguration.newBuilder()
                .endpoint(authenticationProperties.getWssThingsEndpointUrl()).jsonSchemaVersion(JsonSchemaVersion.V_2);

        final AuthenticationProvider<WebSocket> authenticationProvider;

        final ClientCredentialsAuthenticationConfiguration.ClientCredentialsAuthenticationConfigurationBuilder authenticationConfigurationBuilder = ClientCredentialsAuthenticationConfiguration
                .newBuilder().clientId(authenticationProperties.getClientId())
                .clientSecret(authenticationProperties.getClientSecret()).scopes(getScopes())
                .tokenEndpoint(authenticationProperties.getOauthTokenEndpoint());

        authenticationProvider = AuthenticationProviders.clientCredentials(authenticationConfigurationBuilder.build());

        return MessagingProviders.webSocket(builder.build(), authenticationProvider);
    }

    private final List<String> getScopes() {
        final String serviceInstanceId = authenticationProperties.getServiceInstanceId();
        final List<String> res = new ArrayList<String>();
        // res.add("openid");
        // res.add("offline_access");

        /*
         * for (final String name : Arrays.asList("iot-hub", "iot-things",
         * "iot-manager", "iot-rollouts")) {
         * res.add(String.format("service-instance.%s.%s", serviceInstanceId,
         * name)); }
         */

        res.add(String.format("service:iot-manager:%s_iot-manager/full-access", serviceInstanceId));
        res.add(String.format("service:iot-rollouts:%s_rollouts/full-access", serviceInstanceId));
        res.add(String.format("service:iot-hub-prod:t%s_hub/full-access", serviceInstanceId.replace("-", "")));
        res.add(String.format("service:iot-things-eu-1:%s_things/full-access", serviceInstanceId));

        return res;
    }
}
