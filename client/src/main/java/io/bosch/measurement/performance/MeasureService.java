package io.bosch.measurement.performance;

import java.util.concurrent.ExecutionException;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.json.JsonPointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import io.bosch.measurement.ditto.AuthenticationProperties;
import io.bosch.measurement.ditto.DittoClientConfig;
import io.bosch.measurement.ditto.DittoService;
import io.bosch.measurement.status.MeasurementStatus;
import io.bosch.measurement.status.StatusService;

@Service
public class MeasureService {

    private static final Logger LOG = LoggerFactory.getLogger(MeasureService.class);

    @Autowired
    @Qualifier(DittoClientConfig.LIVE_CLIENT)
    DittoClient liveClient;

    @Autowired
    @Qualifier(DittoClientConfig.TWIN_CLIENT)
    DittoClient twinClient;

    @Autowired
    AuthenticationProperties authenticationProperties;

    // agent will respond to featureUpdate on this id..
    private static final String FEATURE_ID_FROM_AGENT = "measure-performance-feature";
    private static final JsonPointer REQUEST_PATH = JsonPointer.of("status/request");
    private static final JsonPointer RESPONSE_PATH = JsonPointer.of("status/response");

    private DittoService dittoService;

    @EventListener(ApplicationReadyEvent.class)
    public void onInit() throws InterruptedException, ExecutionException {
        dittoService = new DittoService(twinClient, authenticationProperties.getDeviceId());
        dittoService.createFeatureIfNotPresent(FEATURE_ID_FROM_AGENT);
        dittoService.registerForFeatureChange(FEATURE_ID_FROM_AGENT, this::onFeatureUpdate);
    }

    private void onFeatureUpdate(final Change change) {
        final String path = change.getPath().toString();
        change.getValue().ifPresent(value -> {
            if (path.equals(REQUEST_PATH.toString())) {
                LOG.info("Request has reached ditto: {}", value);
                StatusService.onDelivered(value);
            } else if (path.equals(RESPONSE_PATH.toString())) {
                LOG.info("Received a response from edge: {}", value);
                StatusService.onEdgeResponseReceived(value);
            }
        });
    }

    public String measureUsingEvents(final Long count) {
        return "not implemented";
    }

    public MeasurementStatus measureUsingFeature(final Long count) {
        final MeasurementStatus m = StatusService.start(count);
        for (Long i = 0L; i < count; i++) {
            dittoService.updateFeature(FEATURE_ID_FROM_AGENT, REQUEST_PATH, new MeasurementData(m.getId(), i));
        }
        return m;
    }


    public MeasurementStatus getStatus(final String id) {
        // calculate the received events from the feature update.
        return StatusService.get(id);
    }

}
