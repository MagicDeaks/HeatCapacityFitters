package com.magicdeaks.heatcapacity.records;

import com.magicdeaks.heatcapacity.util.Deviations;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public record FitResult(double[] coefficients, double pctRMS, int iterations, Deviations deviations)
        implements Serializable {
    public FitResult(double[] coefficients, double pctRMS) {
        this(coefficients, pctRMS, 0, new Deviations());
    }

    public FitResult(double[] coefficients, double pctRMS, Deviations deviations) {
        this(coefficients, pctRMS, 0, deviations);
    }

    public FitResult(double[] coefficients, double pctRMS, int iterations) {
        this(coefficients, pctRMS, iterations, new Deviations());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FitResult that = (FitResult) o;

        return Double.compare(that.pctRMS, pctRMS) == 0
                && iterations == that.iterations
                && Arrays.equals(coefficients, that.coefficients)
                && Objects.equals(deviations, that.deviations);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(pctRMS, iterations, deviations);
        result = 31 * result + Arrays.hashCode(coefficients);
        return result;
    }
}
