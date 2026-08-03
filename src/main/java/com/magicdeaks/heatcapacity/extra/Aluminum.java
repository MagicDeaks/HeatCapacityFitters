package com.magicdeaks.heatcapacity.extra;

import java.util.Arrays;

public abstract class Aluminum {
  private static final double CRITICAL_TEMP = 1.163;
  private static final double SOMMERFELD_GAMMA = 1.35e-3;
  private static final double DEBYE_BETA = 2.60e-5;
  private static final double REDUCED_GAP_ZERO = 1.762;

  public static double[] calculateBCSHeatCapacities(double[] temperatures) {
    if (temperatures == null) {
      throw new IllegalArgumentException("Temperature array cannot be null.");
    }

    return Arrays.stream(temperatures).map(Aluminum::calculateTotalHeatCapacity).toArray();
  }

  private static double calculateTotalHeatCapacity(double t) {
    if (t <= 0.0) {
      return 0.0;
    }

    return latticeHeatCapacity(t) + bcsHeatCapacity(t);
  }

  private static double latticeHeatCapacity(double t) {
    return DEBYE_BETA * Math.pow(t, 3);
  }

  private static double bcsHeatCapacity(double t) {
    if (t >= CRITICAL_TEMP) {
      return SOMMERFELD_GAMMA * t;
    }

    double reducedT = t / CRITICAL_TEMP;

    double deltaSq = calculateDeltaSquared(reducedT);

    double h = 1e-5;
    double deltaSqPlus = calculateDeltaSquared(reducedT + h);
    double deltaSqMinus = calculateDeltaSquared(Math.max(1e-6, reducedT - h));
    double dDeltaSqDt = (deltaSqPlus - deltaSqMinus) / (2.0 * h);

    double integral = integrateReducedBCS(reducedT, deltaSq, dDeltaSqDt);

    double prefactor =
        (3.0 * SOMMERFELD_GAMMA * CRITICAL_TEMP) / (Math.PI * Math.PI * reducedT * reducedT);

    return prefactor * integral;
  }

  private static double calculateDeltaSquared(double reducedT) {
    if (reducedT >= 1.0) {
      return 0.0;
    }

    double arg = 1.74 * Math.sqrt((1.0 / reducedT) - 1.0);
    double tanhVal = Math.tanh(arg);
    double deltaVal = REDUCED_GAP_ZERO * tanhVal;
    return deltaVal * deltaVal;
  }

  private static double integrateReducedBCS(double reducedT, double deltaSq, double dDeltaSqDt) {
    int n = 1000;
    double yMin = 0.0;
    double yMax = 15.0;
    double h = (yMax - yMin) / n;

    double sum =
        integrand(yMin, reducedT, deltaSq, dDeltaSqDt)
            + integrand(yMax, reducedT, deltaSq, dDeltaSqDt);

    for (int i = 1; i < n; i++) {
      double y = yMin + i * h;
      double val = integrand(y, reducedT, deltaSq, dDeltaSqDt);
      sum += (i % 2 == 0 ? 2.0 : 4.0) * val;
    }

    return sum * (h / 3.0);
  }

  private static double integrand(double y, double reducedT, double deltaSq, double dDeltaSqDt) {
    double x = Math.sqrt(y * y + deltaSq);

    double z = x / (2.0 * reducedT);
    double coshVal = Math.cosh(z);
    double weight = 1.0 / (4.0 * coshVal * coshVal);

    double term = (x * x) - (reducedT / 2.0) * dDeltaSqDt;

    return weight * term;
  }
}
