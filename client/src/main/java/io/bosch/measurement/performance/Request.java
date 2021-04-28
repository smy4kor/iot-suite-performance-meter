package io.bosch.measurement.performance;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    private String id;
    private int count;

    /**
     * Time gap between each event sent from the edge device in milli-seconds.
     */
    private int delay;
    private Map responseHeaders;
}
