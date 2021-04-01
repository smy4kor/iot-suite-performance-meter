package io.bosch.measurement.performance;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.json.JsonPointer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import io.bosch.measurement.ditto.AuthenticationProperties;
import io.bosch.measurement.ditto.DittoClientConfig;
import io.bosch.measurement.ditto.DittoService;

@Service
public class MeasureService {

    @Autowired
    @Qualifier(DittoClientConfig.LIVE_CLIENT)
    DittoClient liveClient;

    @Autowired
    @Qualifier(DittoClientConfig.TWIN_CLIENT)
    DittoClient twinClient;

    @Autowired
    AuthenticationProperties authenticationProperties;

    // agent will respond to featureUpdate on this id..
    private final String FEATURE_ID_FROM_AGENT = "measure-performance-feature";
    private final String RESPONSE_PROPERTY_PATH = String.format("/features/%s/properties/status/response",
            FEATURE_ID_FROM_AGENT);
    private static final JsonPointer REQUEST_PATH = JsonPointer.of("status/request");

    private DittoService dittoService;

    @PostConstruct
    public void onInit() {
        dittoService = new DittoService(twinClient, authenticationProperties.getDeviceId());
        dittoService.createFeatureIfNotPresent(FEATURE_ID_FROM_AGENT);
    }

    public String measureUsingEvents(final Long count) {
        return "not implemented";
    }

    public String measureUsingFeature(final Long count) {
        final String id = generateId();
        for (Long i = 0L; i < count; i++) {
            dittoService.updateFeature(FEATURE_ID_FROM_AGENT, REQUEST_PATH, new MeasurementData(id, i));
        }
        return id;
    }

    private static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public String getStatus(final String id) {
        // calculate the received events from the feature update.
        return "not implemented";
    }

}
