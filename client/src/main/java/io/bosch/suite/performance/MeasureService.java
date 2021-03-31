package io.bosch.suite.performance;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.twin.TwinFeatureHandle;
import org.eclipse.ditto.model.things.ThingId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.bosch.example.ditto.DittoClientWrapper;
import io.bosch.example.ditto.SubscriptionInfo;

@Service
public class MeasureService {

    @Autowired
    DittoClientWrapper dittoClientWrapper;

    // agent will respond to featureUpdate on this id..
    private final String FEATURE_ID_FROM_AGENT = "measure-performance-feature";
    private final String PROPERTY_PATH = String.format("/features/%s/properties/status/response",
            FEATURE_ID_FROM_AGENT);

    public String measureUsingEvents(final SubscriptionInfo subscriptionInfo, final Long count) {
        return "not implemented yet";
    }

    public String measureUsingFeature(final SubscriptionInfo subscriptionInfo, final Long count) {
        final String id = generateId();
        final DittoClient client = dittoClientWrapper.getClient(subscriptionInfo);
        for (Long i = 0L; i < count; i++) {
            updateFeature(subscriptionInfo, client, new MeasurementData(id, i));
        }
        return id;
    }

    private void updateFeature(final SubscriptionInfo subscriptionInfo, final DittoClient client,
            final MeasurementData measurementData) {
        // for the feature id, call any operation. edge-agent does not care.
        final ThingId thingId = ThingId.of(subscriptionInfo.getThingId());
        final TwinFeatureHandle twinFeatureHandle = client.twin().forId(thingId).forFeature(FEATURE_ID_FROM_AGENT);
        try {
            twinFeatureHandle.putProperty(PROPERTY_PATH, "featureProperty").get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (final ExecutionException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public String getStatus(final String id) {
        // calculate the received events from the feature update.
        return "not implemented";
    }

}
