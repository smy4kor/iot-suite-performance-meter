package io.bosch.measurement.performance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeasurementData {
    private String id;
    private int expected;
    private int current;
}
