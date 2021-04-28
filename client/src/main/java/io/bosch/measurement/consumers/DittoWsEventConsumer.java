package io.bosch.measurement.consumers;

import java.io.IOException;
import java.util.function.Consumer;

import org.eclipse.ditto.client.live.messages.RepliableMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bosch.measurement.performance.Response;

public class DittoWsEventConsumer extends ConsumerBase implements Consumer<RepliableMessage<?, Object>> {

    private static final Logger LOG = LoggerFactory.getLogger(DittoWsEventConsumer.class);

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
}