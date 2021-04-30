package io.bosch.measurement.consumers;

import java.io.IOException;
import java.util.function.Consumer;

import org.eclipse.ditto.client.changes.Change;
import org.eclipse.ditto.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bosch.measurement.performance.MeasureService;
import io.bosch.measurement.performance.Response;

public class DittoFeatureEventConsumer extends ConsumerBase implements Consumer<Change> {

    private static final Logger LOG = LoggerFactory.getLogger(DittoFeatureEventConsumer.class);

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
            if (request.getId().equals(data.getId())) {
                counter.accept(data);
            } else {
                LOG.error("Received unknown event {}. Expecting {}", data, request.getId());
            }
        } catch (final IOException e) {
            LOG.error("Exception while parsing {}, {}", value, e);
        }
    }



}