package io.bosch.measurement.performance;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.ditto.client.live.messages.RepliableMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

class DittoWsEventConsumer implements Consumer<RepliableMessage<?, Object>> {
    private final int expectedCount;
    private final Consumer<Duration> targetConsumer;
    private long startTime;
    private final List<Response> received;
    private static final Logger LOG = LoggerFactory.getLogger(DittoWsEventConsumer.class);

    public DittoWsEventConsumer(final int expectedCount, final Consumer<Duration> targetConsumer) {
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

            LOG.info("MessageOrderId={} received. {} out of {} completed after {}ms", data.getCurrent(), receivedCount,
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