package io.bosch.measurement.consumers;

import com.google.common.collect.Ordering;
import io.bosch.measurement.performance.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Counter {
    private final String id;
    private final int expectedCount;
    private int duplicateReceivedCount = 0;
    private final int delay;
    private final Set<Response> received;
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
        // private List<Integer> messages;
        private String timeTaken;

        /**
         * This will subtract the delay added to the response.
         */
        private String timePerMessage;
    }

    public Counter(final String id, final int expectedCount, final int delay) {
        this.id = id;
        this.expectedCount = expectedCount;
        this.delay = delay;
        this.received = new HashSet<>(expectedCount);
        this.startTime = System.currentTimeMillis();
    }

    public void accept(final Response data) {
        if (received.add(data)) {
            duplicateReceivedCount++;
        } else {
            lastReceivedEventTime = System.currentTimeMillis();
            if (data.getCurrent() >= data.getExpected()) {
                final String elapsedDuration = prettify(lastReceivedEventTime - startTime);
                LOG.info("Received {} events in {}. Request id {}", expectedCount, elapsedDuration, id);
            }
        }
    }

    public Status getStatus() {
        if (received.isEmpty()) {
            return null;
        }
        final long elapsedTimeWithDelay = lastReceivedEventTime - startTime;
        final boolean completed = expectedCount == received.size();

        final List<Integer> indexReceived = received.stream().map(x -> x.getCurrent()).collect(Collectors.toList());
        final boolean isInOrder = Ordering.natural().isOrdered(indexReceived);

        final Status status = Status.builder()//
                .id(id)//
                .expected(expectedCount) //
                .received(received.size())//
                .completed(completed)//
                .inOrder(isInOrder)//
                // .messages(indexReceived)//
                .timeTaken(prettify(elapsedTimeWithDelay))//
                .build();

        final long averageTimePerMsg = (lastReceivedEventTime - startTime - (received.size() * delay))
                / received.size();
        status.setTimePerMessage(prettify(averageTimePerMsg));
        return status;
    }

    private String prettify(final long milliSeconds) {
        final Duration duration = new Duration(milliSeconds); // in milliseconds
        final PeriodFormatter formatter = new PeriodFormatterBuilder()//
                .appendMinutes()//
                .appendSuffix("m ") //
                .appendSeconds()//
                .appendSuffix("s ")//
                .appendMillis()//
                .appendSuffix("ms")//
                .toFormatter();
        return formatter.print(duration.toPeriod());
    }

}
