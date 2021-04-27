package io.bosch.measurement.performance;

import java.io.IOException;
import java.util.function.Consumer;

import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

class DittoFeatureEventConsumer implements Consumer<Change> {

    private static final Logger LOG = LoggerFactory.getLogger(DittoFeatureEventConsumer.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Request request;
    private Counter counter;

    @Override
    public void accept(final Change change) {
        final String path = change.getPath().toString();
        change.getValue().ifPresent(value -> {
            if (path.equals(MeasureService.REQUEST_PATH.toString())) {
                LOG.info("Request has reached ditto: {}", value);
            } else if (path.equals(MeasureService.RESPONSE_PATH.toString())) {
                process(value);
            }
        });
    }

    public void process(final JsonValue value) {
        try {
            final Response data = objectMapper.readValue(value.toString(), Response.class);
            LOG.info("Received {}", data);
            if (request.getId().equals(data.getId())) {
                counter.accept(data);
            }
        } catch (final IOException e) {
            LOG.error("Exception while parsing {}, {}", value, e);
        }
    }

    public void start(final Request request) {
        this.counter = new Counter(request.getId(), request.getCount());
        this.request = request;
    }
}