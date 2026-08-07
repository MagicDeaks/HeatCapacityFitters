package com.magicdeaks.heatcapacity.records;

import java.io.Serializable;

public record HeatCapacityData(double[] temperatures, double[] heatCapacities)
        implements Serializable {
    public HeatCapacityData {
        if (temperatures == null || heatCapacities == null) {
            throw new IllegalArgumentException("Data arrays cannot be null.");
        }
        if (temperatures.length != heatCapacities.length) {
            throw new IllegalArgumentException(
                    "Temperatures and heat capacities must have the same length.");
        }
    }
}
