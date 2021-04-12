package io.bosch.measurement.status;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
public class MeasurementStatus {
    private String id;
    private int packetSent;
    /**
     * Number of events which confirmed that the request feature was
     * successfully updated and edge device should have received it.
     */
    private int packetDelivered = 0;

    /**
     * Number of responses received from the edge device.
     */
    private int confirmedResponse = 0;
    private LocalDateTime startedAt;
    private LocalDateTime endAt;

    public boolean isFinished() {
        return this.confirmedResponse == this.packetSent;
    }

    public String getTimeTaken() {
        final LocalDateTime end = endAt == null ? LocalDateTime.now() : endAt;
        return Math.round(ChronoUnit.MILLIS.between(startedAt, end) / 1000.0) + "sec";
    }

}
