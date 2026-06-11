package com.magicdeaks.heatcapacity.util;

import java.util.function.Function;

public abstract class HighTFunctions {
    private static double debyeIntegrand(double x) {
        if (x == 0.0) {
            return 0.0;
        }

        double expMinus = Math.exp(-x);
        return Math.pow(x, 4) * expMinus / ((1 - expMinus) * (1 - expMinus));
    }

    public static double simpsonsApprox(Function<Double, Double> integrand, double a, double b, int intervals) {
        if (intervals <= 0 || intervals % 2 != 0) {
            throw new IllegalArgumentException("intervals must be a positive even integer");
        }

        double deltaX = (b - a) / intervals;
        double sum = 0;

        for (int i = 0; i <= intervals; i++) {
            if (i == 0 || i == intervals) {
                sum += integrand.apply(a + i*deltaX);
            }
            else if (i % 2 == 0) {
                sum += 2 * integrand.apply(a + i*deltaX);
            }
            else {
                sum += 4 * integrand.apply(a + i*deltaX);
            }
        }

        return sum * deltaX / 3;
    }

    public static double calculateDebye(double debyeTemp, double temp, int intervals) {
        double coefficient = 9 * 8.314472 * temp * temp * temp / (debyeTemp * debyeTemp * debyeTemp);
        double integral = simpsonsApprox(HighTFunctions::debyeIntegrand, 0, debyeTemp / temp, intervals);

        return coefficient * integral;
    }

    public static double calculateEinstein(double einsteinTemp, double temp) {
        double ratio = einsteinTemp / temp;

        double expMinus = Math.exp(-ratio);
        double intermediate = expMinus / ((1 - expMinus) * (1 - expMinus));

        return 3 * 8.314472 * (ratio * ratio) * intermediate;
    }
}
