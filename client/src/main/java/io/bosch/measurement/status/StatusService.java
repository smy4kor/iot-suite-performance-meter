package io.bosch.measurement.status;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.ditto.json.JsonValue;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.bosch.measurement.performance.MeasurementData;

@Service
public class StatusService {
    private static final Map<String, MeasurementStatus> allStatus = new HashMap<>();

    public MeasurementStatus start(final Long packets) {
        final MeasurementStatus m = new MeasurementStatus();
        m.setId(generateId());
        m.setStartedAt(LocalDateTime.now());
        m.setPacketSent(packets);
        allStatus.put(m.getId(), m);
        return m;
    }

    private static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public MeasurementStatus get(final String id) {
        return allStatus.get(id);
    }

    public void onDelivered(final JsonValue value) {
        final MeasurementData data = convert(value);
        final MeasurementStatus status = allStatus.get(data.getId());
        status.setPacketDelivered(status.getPacketDelivered() + 1);
    }

    public void onEdgeResponseReceived(final JsonValue value) {
        final MeasurementData data = convert(value);
        final MeasurementStatus status = allStatus.get(data.getId());
        status.setConfirmedResponse(status.getConfirmedResponse() + 1);
        if (status.isFinished()) {
            status.setEndAt(LocalDateTime.now());
        }
    }

    private MeasurementData convert(final JsonValue value) {
        try {
            return new ObjectMapper().readValue(value.toString(), MeasurementData.class);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
