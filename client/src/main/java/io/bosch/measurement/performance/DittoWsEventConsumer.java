package io.bosch.measurement.performance;

import java.io.IOException;
import java.util.function.Consumer;

import org.eclipse.ditto.client.live.messages.RepliableMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.bosch.measurement.performance.Counter.Status;

class DittoWsEventConsumer implements Consumer<RepliableMessage<?, Object>> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Request request;
    private Counter counter = new Counter("", 0);
    private static final Logger LOG = LoggerFactory.getLogger(DittoWsEventConsumer.class);

    public void reset(final Request request) {
        this.counter = new Counter(request.getId(), request.getCount());
        this.request = request;
    }

    @Override
    public void accept(final RepliableMessage<?, Object> message) {
        final String msg = message.getPayload().get().toString();
        try {
            final Response data = objectMapper.readValue(msg, Response.class);
            if (request.getId().equals(data.getId())) {
                counter.accept(data);
            }

        } catch (final IOException e) {
            LOG.error("Exception while parsing {}, {}", msg, e);
        }
    }

    public Status getStatus() {
        return this.counter.getStatus();
    }
}