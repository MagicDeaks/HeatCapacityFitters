package com.magicdeaks.heatcapacity.records;

public record FitResult(double[] coefficients, double pctRMS, int iterations) {
    public FitResult(double[] coefficients, double pctRMS) {
        this(coefficients, pctRMS, 0);
    }
}
