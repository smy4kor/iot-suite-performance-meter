package io.bosch.measurement.performance;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class Counter {
    private final String id;
    private final int expectedCount;
    private final List<Response> received;
    private final long startTime;
    private long lastReceivedEventTime;

    private static final Logger LOG = LoggerFactory.getLogger(Counter.class);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Status {
        private String id;
        private int expected;
        private int received;
        private boolean completed;
        private boolean inOrder;
        private String duration;
    }

    public Counter(final String id, final int expectedCount) {
        this.id = id;
        this.expectedCount = expectedCount;
        this.received = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
    }

    public void accept(final Response data) {
        received.add(data);
        lastReceivedEventTime = System.currentTimeMillis();
        if (received.size() == expectedCount) {
            final Duration elapsedDuration = Duration.ofMillis(lastReceivedEventTime - startTime);
            LOG.info("Received {} events in {}. Request id {}", expectedCount, elapsedDuration, id);
        }
    }

    public Status getStatus() {
        final String elapsedDuration = String.format("%sms", Duration.ofMillis(lastReceivedEventTime - startTime));
        final boolean completed = expectedCount == received.size();
        return Status.builder()//
                .id(id)//
                .expected(expectedCount) //
                .received(received.size())//
                .completed(completed)//
                .duration(elapsedDuration).build();
    }

}
