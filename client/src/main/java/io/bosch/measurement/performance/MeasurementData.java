package io.bosch.measurement.performance;

public class MeasurementData {
    private String id;
    private Long serialNumber;
    private String sender;

    public MeasurementData() {

    }

    public MeasurementData(final String id, final Long serialNumber) {
        this.id = id;
        this.serialNumber = serialNumber;

        // edge-agent will process it only if the sender is client.
        this.sender = "client";
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Long getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(final Long serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(final String sender) {
        this.sender = sender;
    }

}
