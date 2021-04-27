package io.bosch.measurement.performance;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Counter {
    private final String id;
    private final int expectedCount;
    private final List<Response> received;
    private final long startTime;

    private static final Logger LOG = LoggerFactory.getLogger(Counter.class);

    public Counter(final String id, final int expectedCount) {
        this.id = id;
        this.expectedCount = expectedCount;
        this.received = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
    }

    public void accept(final Response data) {
        received.add(data);
        if(received.size() == expectedCount) {
            final Duration elapsedDuration = Duration.ofMillis(System.currentTimeMillis() - startTime);
            LOG.info("Received {} events in {}ms", expectedCount, elapsedDuration);
        }
    }

}
