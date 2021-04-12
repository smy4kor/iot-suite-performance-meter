package io.bosch.measurement.performance;

import io.bosch.measurement.ditto.AuthenticationProperties;
import io.bosch.measurement.ditto.DittoThingClient;
import io.bosch.measurement.status.MeasurementStatus;
import io.bosch.measurement.status.StatusService;
import lombok.RequiredArgsConstructor;
import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.live.messages.RepliableMessage;
import org.eclipse.ditto.json.JsonPointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class MeasureService {

    private static final Logger LOG = LoggerFactory.getLogger(MeasureService.class);

    private final DittoClient dittoClient;
    private final AuthenticationProperties authenticationProperties;

    // agent will respond to featureUpdate on this id..
    private static final String FEATURE_ID = "measure-performance-feature";
    private static final JsonPointer REQUEST_PATH = JsonPointer.of("status/request");
    private static final JsonPointer RESPONSE_PATH = JsonPointer.of("status/response");

    private DittoThingClient thingClient;

    @RequiredArgsConstructor
    private static class CountingConsumer implements Consumer<RepliableMessage<?, Object>> {
        private final int targetCount;
        private final Consumer<Duration> targetConsumer;

        private int receivedCount = 0;
        private long lastReceivedTime;
        private long startTime;

        @Override
        public void accept(RepliableMessage<?, Object> message) {
            receivedCount++;
            if (LOG.isDebugEnabled() && receivedCount%10 == 0) {
                LOG.debug("Events received: {}", receivedCount);
            }
            if ( receivedCount >= targetCount && targetConsumer != null) {
                lastReceivedTime = System.currentTimeMillis();
                Duration d = Duration.ofMillis(lastReceivedTime - startTime);
                targetConsumer.accept(d);
            }
        }

        public void start() {
            this.startTime = System.currentTimeMillis();
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onInit() throws InterruptedException, ExecutionException {
        thingClient = new DittoThingClient(dittoClient, authenticationProperties.getDeviceId());
        thingClient.createFeatureIfNotPresent(FEATURE_ID);
        thingClient.registerForFeatureChange(FEATURE_ID, this::onFeatureUpdate);
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

    public String measureUsingEvents(final int count) {
        thingClient.deregisterForMeterEvents();
        CountingConsumer handler = new CountingConsumer(count, duration -> {
            thingClient.deregisterForMeterEvents();
            LOG.info("Time elapsed for {} event is {}s ({}ms/event)",
                    count, duration.toSeconds(), duration.toMillis()/count);
        });
        thingClient.registerForMeterEvents(handler);
        handler.start();
        thingClient.sendStartMessage(FEATURE_ID, count);
        return "Meter started";
    }

    public MeasurementStatus measureUsingFeature(final int count) {
        final MeasurementStatus m = StatusService.start(count);
        for (int i = 0; i < count; i++) {
            thingClient.updateFeature(FEATURE_ID, REQUEST_PATH, new MeasurementData(m.getId(), i));
        }
        return m;
    }


    public MeasurementStatus getStatus(final String id) {
        // calculate the received events from the feature update.
        return StatusService.get(id);
    }

}
