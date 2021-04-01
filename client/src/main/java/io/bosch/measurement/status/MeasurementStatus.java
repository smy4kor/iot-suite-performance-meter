package io.bosch.measurement.status;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class MeasurementStatus {
    private String id;
    private Long packetSent;
    /**
     * Number of events which confirmed that the request feature was
     * successfully updated and edge device should have received it.
     */
    private Long packetDelivered = 0L;

    /**
     * Number of responses received from the edge device.
     */
    private Long confirmedResponse = 0L;
    private LocalDateTime startedAt;
    private LocalDateTime endAt;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Long getPacketSent() {
        return packetSent;
    }

    public void setPacketSent(final Long packetSent) {
        this.packetSent = packetSent;
    }

    public Long getPacketDelivered() {
        return packetDelivered;
    }

    public void setPacketDelivered(final Long packetDelivered) {
        this.packetDelivered = packetDelivered;
    }

    public Long getConfirmedResponse() {
        return confirmedResponse;
    }

    public void setConfirmedResponse(final Long confirmedResponse) {
        this.confirmedResponse = confirmedResponse;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(final LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(final LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public boolean isFinished() {
        return this.confirmedResponse == this.packetSent;
    }

    public String getTimeTaken() {
        final LocalDateTime end = endAt == null ? LocalDateTime.now() : endAt;
        return Math.round(ChronoUnit.MILLIS.between(startedAt, end) / 1000.0) + "sec";
    }

}
