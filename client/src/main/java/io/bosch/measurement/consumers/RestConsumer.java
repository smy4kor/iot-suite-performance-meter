package io.bosch.measurement.consumers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.bosch.measurement.performance.Response;

/**
 * Consumer for consuming the events sent by the device using the rest endpoint
 * of this client application.
 *
 */
@Service
public class RestConsumer extends ConsumerBase {

    private static final Logger LOG = LoggerFactory.getLogger(RestConsumer.class);

    public void accept(final Response response) {
        if (request.getId().equals(response.getId())) {
            counter.accept(response);
        } else {
            LOG.error("Received unknown event {}. Expecting {}", response, request.getId());
        }
    }
}
