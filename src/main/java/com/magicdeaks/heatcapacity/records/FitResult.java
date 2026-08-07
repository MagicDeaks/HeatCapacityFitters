package com.magicdeaks.heatcapacity.records;

import com.magicdeaks.heatcapacity.util.Deviations;

public record FitResult(
    double[] coefficients, double pctRMS, int iterations, Deviations deviations) {
  public FitResult(double[] coefficients, double pctRMS) {
    this(coefficients, pctRMS, 0, null);
  }

  public FitResult(double[] coefficients, double pctRMS, Deviations deviations) {
    this(coefficients, pctRMS, 0, deviations);
  }

  public FitResult(double[] coefficients, double pctRMS, int iterations) {
    this(coefficients, pctRMS, iterations, null);
  }
}
