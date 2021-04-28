package io.bosch.measurement.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.bosch.measurement.consumers.Counter.Status;
import io.bosch.measurement.performance.Request;

public abstract class ConsumerBase {

    final ObjectMapper objectMapper = new ObjectMapper();
    Request request;
    Counter counter = new Counter("", 0, 0);

    public void reset(final Request request) {
        this.counter = new Counter(request.getId(), request.getCount(), request.getDelay());
        this.request = request;
    }

    public Status getStatus() {
        return this.counter.getStatus();
    }
}
