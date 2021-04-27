package io.bosch.measurement.performance;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.client.live.messages.RepliableMessage;
import org.eclipse.ditto.json.JsonPointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

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
    private static final JsonPointer REQUEST_PATH = JsonPointer.of("status/request");
    private static final JsonPointer RESPONSE_PATH = JsonPointer.of("status/response");

    private DittoThingClient thingClient;

    private static class CountingConsumer implements Consumer<RepliableMessage<?, Object>> {
        private final int expectedCount;
        private final Consumer<Duration> targetConsumer;
        private long startTime;
        private final List<Response> received;

        public CountingConsumer(final int expectedCount, final Consumer<Duration> targetConsumer) {
            this.expectedCount = expectedCount;
            this.targetConsumer = targetConsumer;
            this.received = new ArrayList<>();
        }

        @Override
        public void accept(final RepliableMessage<?, Object> message) {
            final String msg = message.getPayload().get().toString();
            try {
                final Response data = new ObjectMapper().readValue(msg, Response.class);
                received.add(data);
                final int receivedCount = received.size();
                final long lastReceivedTime = System.currentTimeMillis();
                final Duration elapsedDuration = Duration.ofMillis(lastReceivedTime - startTime);

                LOG.info("MessageOrderId={} received. {} out of {} completed after {}ms", data.getCurrent(),
                        receivedCount,
                        data.getExpected(), elapsedDuration.toMillis());
                if (LOG.isDebugEnabled() && receivedCount % 10 == 0) {
                    LOG.debug("Events received: {}", receivedCount);
                }
                if (receivedCount >= expectedCount) {

                    targetConsumer.accept(elapsedDuration);
                }
            } catch (final IOException e) {
                LOG.error("Exception while parsing {}, {}", msg, e);
            }
        }

        public void start() {
            this.startTime = System.currentTimeMillis();
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onInit() throws InterruptedException, ExecutionException {
        thingClient = new DittoThingClient(twinClient, liveClient, authenticationProperties.getDeviceId());
        thingClient.createFeatureIfNotPresent(FEATURE_ID);
        thingClient.registerForFeatureChange(FEATURE_ID, this::onFeatureUpdate);
    }

    private void onFeatureUpdate(final Change change) {
        final String path = change.getPath().toString();
        change.getValue().ifPresent(value -> {
            if (path.equals(REQUEST_PATH.toString())) {
                LOG.info("Request has reached ditto: {}", value);
                // StatusService.onDelivered(value);
            } else if (path.equals(RESPONSE_PATH.toString())) {
                LOG.info("Received a response from edge: {}", value);
                // StatusService.onEdgeResponseReceived(value);
            }
        });
    }

    public String measureUsingEvents(final Request request) {
        thingClient.deregisterForMeterEvents();
        final CountingConsumer handler = new CountingConsumer(request.getCount(), duration -> {
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
