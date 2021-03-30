package io.bosch.suite.performance;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MeasureService {

    @Autowired
    DittoService dittoService;

    public String measureUsingEvents(final Long count) {
        return null;
    }

    public String measureUsingFeature(final String featureId, final Long count) {
        final String id = generateId();
        for (Long i = 0L; i < count; i++) {
            dittoService.updateFeature(featureId, new MeasurementData(id, i));
        }
        return id;
    }

    private static String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
