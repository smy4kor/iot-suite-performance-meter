package io.bosch.example.ditto;

public class SubscriptionInfo {
    private final String clientId;
    private final String thingId;
    private final String secret;
    private final String serviceInstanceId;

    public SubscriptionInfo(final String clientId, final String secret, final String serviceInstanceId,
            final String thingId) {
        this.clientId = clientId;
        this.secret = secret;
        this.serviceInstanceId = serviceInstanceId;
        this.thingId = thingId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getSecret() {
        return secret;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public String getThingId() {
        return thingId;
    }

}
