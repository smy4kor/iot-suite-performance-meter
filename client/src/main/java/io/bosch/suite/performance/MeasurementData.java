package io.bosch.suite.performance;

public class MeasurementData {
    private String id;
    private Long serialNumber;

    public MeasurementData() {

    }

    public MeasurementData(final String id, final Long serialNumber) {
        this.id = id;
        this.serialNumber = serialNumber;
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

}
