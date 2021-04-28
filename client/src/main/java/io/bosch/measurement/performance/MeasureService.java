package io.bosch.measurement.performance;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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

import io.bosch.measurement.consumers.Counter.Status;
import io.bosch.measurement.consumers.DittoFeatureEventConsumer;
import io.bosch.measurement.consumers.DittoWsEventConsumer;
import io.bosch.measurement.consumers.RestConsumer;
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

    @Autowired
    RestConsumer restConsumer;

    // agent will respond to featureUpdate on this id..
    private static final String FEATURE_ID = "measure-performance-feature";
    public static final JsonPointer REQUEST_PATH = JsonPointer.of("status/request");
    public static final JsonPointer RESPONSE_PATH = JsonPointer.of("status/response");

    private DittoThingClient thingClient;
    private final DittoFeatureEventConsumer featureEventConsumer = new DittoFeatureEventConsumer();
    private final DittoWsEventConsumer wsEventConsumer = new DittoWsEventConsumer();

    @EventListener(ApplicationReadyEvent.class)
    public void onInit() throws InterruptedException, ExecutionException {
        // featureEventConsumer = new DittoFeatureEventConsumer();
        // wsEventConsumer = new DittoWsEventConsumer();
        thingClient = new DittoThingClient(twinClient, liveClient, authenticationProperties.getDeviceId());
        thingClient.createFeatureIfNotPresent(FEATURE_ID);
        thingClient.registerForFeatureChange(FEATURE_ID, featureEventConsumer);
    }

    public String measureUsingEvents(final Request request) {
        thingClient.deregisterForMeterEvents();
        wsEventConsumer.reset(request);
        thingClient.registerForMeterEvents(wsEventConsumer);
        thingClient.sendStartMessage(FEATURE_ID, request);
        return "Meter started";
    }

    public void measureUsingFeature(final Request request) {
        featureEventConsumer.reset(request);
        thingClient.updateFeature(FEATURE_ID, REQUEST_PATH, request);
    }

    public void measureUsingRest(final Request request) {
        restConsumer.reset(request);
        thingClient.sendStartMessage(FEATURE_ID, request);
    }

    public Map getStatus() {
        final HashMap<String, Status> res = new LinkedHashMap<String, Status>();
        res.put("feature-based", featureEventConsumer.getStatus());
        res.put("event-based", wsEventConsumer.getStatus());
        res.put("rest-based", restConsumer.getStatus());
        return res;
    }

}
