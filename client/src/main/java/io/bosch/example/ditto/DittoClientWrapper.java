package io.bosch.example.ditto;

import java.util.Arrays;
import java.util.List;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.DittoClients;
import org.eclipse.ditto.client.configuration.ClientCredentialsAuthenticationConfiguration;
import org.eclipse.ditto.client.configuration.ClientCredentialsAuthenticationConfiguration.ClientCredentialsAuthenticationConfigurationBuilder;
import org.eclipse.ditto.client.configuration.MessagingConfiguration;
import org.eclipse.ditto.client.configuration.WebSocketMessagingConfiguration;
import org.eclipse.ditto.client.messaging.AuthenticationProvider;
import org.eclipse.ditto.client.messaging.AuthenticationProviders;
import org.eclipse.ditto.client.messaging.MessagingProvider;
import org.eclipse.ditto.client.messaging.MessagingProviders;
import org.eclipse.ditto.model.base.json.JsonSchemaVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.neovisionaries.ws.client.WebSocket;

@Service
public class DittoClientWrapper {

    @Value("${suite.wssThingsEndpointUrl}")
    private String wssThingsEndpointUrl;

    @Value("${suite.oauthTokenEndpoint}")
    private String oauthTokenEndpoint;

    public DittoClient getClient(final SubscriptionInfo info) {
        // to do: optimize by caching the client.
        return DittoClients.newInstance(createDittoConfiguration(info));
    }

    private MessagingProvider createDittoConfiguration(final SubscriptionInfo info) {
        final MessagingConfiguration.Builder messagingBuilder = WebSocketMessagingConfiguration.newBuilder()
                .endpoint(wssThingsEndpointUrl).jsonSchemaVersion(JsonSchemaVersion.V_2);

        return MessagingProviders.webSocket(messagingBuilder.build(), buildAuthenticationProvider(info));
    }

    private AuthenticationProvider<WebSocket> buildAuthenticationProvider(final SubscriptionInfo info) {

        // see DittoClientSupplier from simulator
        final ClientCredentialsAuthenticationConfigurationBuilder builder = ClientCredentialsAuthenticationConfiguration
                .newBuilder().clientId(info.getClientId()) //
                .clientSecret(info.getSecret()) //
                .scopes(getScopes(info.getServiceInstanceId())).tokenEndpoint(oauthTokenEndpoint);

        return AuthenticationProviders.clientCredentials(builder.build());
    }

    private static final List<String> getScopes(final String serviceInstanceId) {
        final List<String> res = Arrays.asList("openid", "offline_access");
        for (final String name : Arrays.asList("iot-hub", "iot-things", "iot-manager", "iot-rollouts")) {
            res.add(String.format("service-instance.%s.%s", serviceInstanceId, name));
        }
        return res;
    }

}
