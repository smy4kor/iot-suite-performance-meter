package io.bosch.measurement.performance;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MeasurementData {
    private String id;
    private int serialNumber;
}
