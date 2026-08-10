package com.magicdeaks.heatcapacity.records;

import java.io.Serializable;

public record ThermFunctions(
        double[] temperatures,
        double[] heatCapacities,
        double[] enthalpies,
        double[] entropies,
        double[] gibbs)
        implements Serializable {
    public ThermFunctions {
        if (temperatures == null
                || heatCapacities == null
                || enthalpies == null
                || entropies == null
                || gibbs == null) {

            throw new IllegalArgumentException("Data arrays cannot be null.");
        }
        if (temperatures.length != heatCapacities.length
                || heatCapacities.length != enthalpies.length
                || enthalpies.length != entropies.length
                || entropies.length != gibbs.length) {

            throw new IllegalArgumentException("Data arrays must be the same length.");
        }
    }
}
