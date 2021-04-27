package io.bosch.measurement.performance;

import java.util.concurrent.ExecutionException;

import org.eclipse.ditto.client.DittoClient;
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
import io.bosch.measurement.ditto.DittoThingClient;

@Service
public class MeasureService {

    private static final Logger LOG = LoggerFactory.getLogger(MeasureService.class);

    @Autowired
    @Qualifier(DittoClientConfig.TWIN_CLIENT)
    private DittoClient twinClient;

    @Autowired
    @Qualifier(DittoClientConfig.LIVE_CLIENT)
    private DittoClient liveClient;

    @Autowired
    private AuthenticationProperties authenticationProperties;

    // agent will respond to featureUpdate on this id..
    private static final String FEATURE_ID = "measure-performance-feature";
    public static final JsonPointer REQUEST_PATH = JsonPointer.of("status/request");
    public static final JsonPointer RESPONSE_PATH = JsonPointer.of("status/response");

    private DittoThingClient thingClient;



    @EventListener(ApplicationReadyEvent.class)
    public void onInit() throws InterruptedException, ExecutionException {
        thingClient = new DittoThingClient(twinClient, liveClient, authenticationProperties.getDeviceId());
        thingClient.createFeatureIfNotPresent(FEATURE_ID);
        thingClient.registerForFeatureChange(FEATURE_ID, new DittoFeatureEventConsumer());
    }

    public String measureUsingEvents(final Request request) {
        thingClient.deregisterForMeterEvents();
        final DittoWsEventConsumer handler = new DittoWsEventConsumer(request.getCount(), duration -> {
            thingClient.deregisterForMeterEvents();
            LOG.info("Time elapsed for {} event is {}s ({}ms). Average of {}ms per event", request.getCount(),
                    duration.getSeconds(), duration.toMillis(), duration.toMillis() / request.getCount());
        });
        thingClient.registerForMeterEvents(handler);
        handler.start();
        thingClient.sendStartMessage(FEATURE_ID, request);
        return "Meter started";
    }

    public void measureUsingFeature(final Request request) {
        thingClient.updateFeature(FEATURE_ID, REQUEST_PATH, request);
    }

}
