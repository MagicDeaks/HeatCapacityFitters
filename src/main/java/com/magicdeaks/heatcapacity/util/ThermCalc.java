package com.magicdeaks.heatcapacity.util;

import static com.magicdeaks.heatcapacity.util.PolyCurveFitter.evaluatePolynomial;

import com.magicdeaks.heatcapacity.session.AnalysisSession;
import java.util.Arrays;
import java.util.stream.IntStream;

public abstract class ThermCalc {
  public static double[] calculateTemperatures(double minT, double maxT, double dT) {
    int points = (int) ((maxT - minT) / dT);

    double[] temperatures = new double[points];
    for (int i = 0; i < points; i++) {
      temperatures[i] = dT * (i + 1) + minT;
    }

    return temperatures;
  }

  public static double[] calculateHeatCapacity(
      AnalysisSession session,
      double lowOverlapT,
      double highOverlapT,
      double minT,
      double maxT,
      double dT) {

    double[] temperatures = calculateTemperatures(minT, maxT, dT);

    double[] lowT = getTRange(temperatures, minT, lowOverlapT);
    double[] midT = getTRange(temperatures, lowOverlapT, highOverlapT);
    double[] highT = getTRange(temperatures, highOverlapT, maxT);

    double[] lowHC = session.getLowTModel().value(lowT, session.getLowTFit().coefficients());
    double[] highHC = session.getHighTModel().value(highT, session.getHighTFit().coefficients());

    int[] midModel = session.getMidTModel()[session.getMidTSelect()];
    double[] midCoeffs = session.getMidTFit()[session.getMidTSelect()].coefficients();

    double[][] pairs = new double[midModel.length][];

    for (int i = 0; i < midModel.length; i++) {
      pairs[i] = new double[] {midCoeffs[i], midModel[i]};
    }

    double[] midHC = evaluatePolynomial(midT, pairs);

    int points = (int) ((maxT - minT) / dT);

    double[] fullHC = new double[points];

    if (lowHC.length + midHC.length + highHC.length != points) {
      throw new IllegalArgumentException("Array lengths do not align.");
    }

    for (int i = 0; i < lowHC.length; i++) {
      fullHC[i] = lowHC[i];
    }
    for (int j = 0; j < midHC.length; j++) {
      fullHC[lowHC.length + j] = midHC[j];
    }
    for (int k = 0; k < highHC.length; k++) {
      fullHC[lowHC.length + midHC.length + k] = highHC[k];
    }

    return fullHC;
  }

  public static double[] calculateEnthalpies(double[] temperatures, double[] heatCapacities) {
    if (temperatures.length != heatCapacities.length) {
      throw new IllegalArgumentException("Arguments must have same length.");
    }

    double[] enthalpies = new double[temperatures.length];

    for (int i = 0; i < temperatures.length; i++) {
      if (i == 0) {
        enthalpies[i] = heatCapacities[i] * temperatures[i];
      } else {
        enthalpies[i] =
            (heatCapacities[i] - heatCapacities[i - 1]) * (temperatures[i] - temperatures[i - 1])
                + enthalpies[i - 1];
      }
    }

    return enthalpies;
  }

  public static double[] calculateEntropies(double[] temperatures, double[] heatCapacities) {
    if (temperatures.length != heatCapacities.length) {
      throw new IllegalArgumentException("Arguments must have same length.");
    }

    double[] scaledHeatCapacities = new double[temperatures.length];

    for (int i = 0; i < temperatures.length; i++) {
      scaledHeatCapacities[i] = heatCapacities[i] / temperatures[i];
    }

    double[] entropies = new double[temperatures.length];

    for (int i = 0; i < temperatures.length; i++) {
      if (i == 0) {
        entropies[i] = scaledHeatCapacities[i] * temperatures[i];
      } else {
        entropies[i] =
            (scaledHeatCapacities[i] - scaledHeatCapacities[i - 1])
                    * (temperatures[i] - temperatures[i - 1])
                + entropies[i - 1];
      }
    }

    return entropies;
  }

  public static double[] calculateGibbs(
      double[] temperatures, double[] enthalpies, double[] entropies) {
    if (temperatures.length != enthalpies.length || enthalpies.length != entropies.length) {
      throw new IllegalArgumentException("Arguments must have same length.");
    }

    double[] gibbs = new double[temperatures.length];

    for (int i = 0; i < temperatures.length; i++) {
      gibbs[i] = -(enthalpies[i] - temperatures[i] * entropies[i]) / temperatures[i];
    }

    return gibbs;
  }

  public static double[] getTRange(double[] rawTemps, double minT, double maxT) {
    int minIdx =
        IntStream.range(0, rawTemps.length).filter(i -> rawTemps[i] >= minT).findFirst().orElse(0);
    int maxIdx =
        IntStream.range(0, rawTemps.length)
            .filter(i -> rawTemps[i] <= maxT)
            .reduce((_, second) -> second)
            .orElse(rawTemps.length - 1);

    double[] temps = Arrays.copyOfRange(rawTemps, minIdx, maxIdx + 1);

    return temps;
  }
}
