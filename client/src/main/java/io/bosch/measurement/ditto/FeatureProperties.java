/**
 * Copyright (c) 2020 Bosch.IO GmbH, Germany. All rights reserved.
 */
package io.bosch.measurement.ditto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * Represents the feature descriptions
 */
public class FeatureProperties {

    @NotBlank
    private String featureId;
    @NotEmpty
    private String[] featureDefinitions;
    private String[] types;

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(final String featureId) {
        this.featureId = featureId;
    }

    public String[] getFeatureDefinitions() {
        return featureDefinitions;
    }

    public void setFeatureDefinitions(final String[] featureDefinitions) {
        this.featureDefinitions = featureDefinitions;
    }

    public String[] getTypes() {
        return types;
    }

    public void setTypes(final String[] types) {
        this.types = types;
    }
}
