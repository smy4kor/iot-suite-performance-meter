package io.bosch.measurement.ditto;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.ditto.client.DittoClient;
import org.eclipse.ditto.client.management.ThingHandle;
import org.eclipse.ditto.client.twin.TwinFeatureHandle;
import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.FeatureProperties;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DittoService {
    private static final Logger LOG = LoggerFactory.getLogger(DittoService.class.getName());
    private final DittoClient dittoClient;
    private final ThingId thingId;

    public DittoService(final DittoClient dittoClient, final String deviceId) {
        thingId = ThingId.of(deviceId);
        this.dittoClient = dittoClient;
    }

    public Optional<Thing> getThing() {
        Thing responseThing = null;
        try {
            responseThing = dittoClient.twin().forId(thingId).retrieve().get(3, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            LOG.error("Cannot get thing with id={}", thingId);
            LOG.error("Client-Error:", e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return Optional.ofNullable(responseThing);
    }

    public void createFeatureIfNotPresent(final String featureId) {
        try {
            dittoClient.twin().forId(thingId).forFeature(featureId).retrieve().get();
        } catch (final Exception e) {
            final FeatureProperties featureProperties = FeatureProperties.newBuilder().build();
            final Feature feature = ThingsModelFactory.newFeatureBuilder().properties(featureProperties)
                    .withId(featureId).build();
            createFeature(feature);
        }
    }

    public Feature createFeature(final Feature feature) {
        try {
            return createFeatureWithClient(feature).get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("{}: Failed to create feature.", thingId.getName(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (final ExecutionException e) {
            LOG.error("{}: Failed to create feature.", thingId.getName(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public void updateFeature(final String featureId, final JsonPointer path, final Object value) {
        // for the feature id, call any operation. edge-agent does not care.
        final TwinFeatureHandle twinFeatureHandle = dittoClient.twin().forId(thingId).forFeature(featureId);
        final JsonValue asJsonValue = getAsJsonValue(value);
        try {
            twinFeatureHandle.putProperty(path, asJsonValue).get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("{}: Failed to update feature {}.", thingId.getName(), featureId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (final ExecutionException e) {
            LOG.error("{}: Failed to update feature {}.", thingId.getName(), featureId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private CompletableFuture<Feature> createFeatureWithClient(final Feature feature) {
        final ThingHandle<TwinFeatureHandle> thingHandle = dittoClient.twin().forId(thingId);
        LOG.debug("{}: Create/Update feature: {}", thingId.getName(), feature.toJson());
        return thingHandle.putFeature(feature).thenCompose(aVoid -> thingHandle.forFeature(feature.getId()).retrieve())
                .whenComplete((responseThing, throwable) -> {
                    if (throwable != null) {
                        LOG.error("{}: Create/Update thing Failed: {}", thingId.getName(), throwable.getMessage());
                    }
                });
    }

    private static JsonValue getAsJsonValue(final Object value) {
        try {
            return JsonFactory.readFrom(new ObjectMapper().writeValueAsString(value));
        } catch (final JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create measurement request property.");
        }
    }

}
