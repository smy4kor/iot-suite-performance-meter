package io.bosch.measurement.status;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.ditto.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.bosch.measurement.performance.MeasurementData;

public class StatusService {
    private static final Logger LOG = LoggerFactory.getLogger(StatusService.class);
    private static final Map<String, MeasurementStatus> statusCache = new ConcurrentHashMap<>();

    public static synchronized MeasurementStatus start(final int packets) {
        final MeasurementStatus m = new MeasurementStatus();
        m.setId(generateId());
        m.setStartedAt(LocalDateTime.now());
        m.setPacketSent(packets);
        statusCache.put(m.getId(), m);
        return m;
    }

    private static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static synchronized MeasurementStatus get(final String id) {
        return statusCache.get(id);
    }

    public static synchronized void onDelivered(final JsonValue value) {
        getFrom(value).ifPresent(data -> {
            data.setPacketDelivered(data.getPacketDelivered() + 1);
        });
    }

    public static synchronized void onEdgeResponseReceived(final JsonValue value) {
        getFrom(value).ifPresent(data -> {
            data.setConfirmedResponse(data.getConfirmedResponse() + 1);
            if (data.isFinished()) {
                data.setEndAt(LocalDateTime.now());
            }
        });
    }

    private static synchronized Optional<MeasurementStatus> getFrom(final JsonValue value) {
        try {
            final MeasurementData d = new ObjectMapper().readValue(value.toString(), MeasurementData.class);
            final Optional<MeasurementStatus> val = Optional.ofNullable(statusCache.get(d.getId()));
            if (!val.isPresent()) {
                LOG.error("Cannot find data for id {}", d.getId());
            }
            return val;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
