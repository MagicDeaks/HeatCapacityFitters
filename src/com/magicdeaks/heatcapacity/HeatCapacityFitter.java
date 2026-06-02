package com.magicdeaks.heatcapacity;

import java.util.EnumMap;
import java.util.Map;

public abstract class HeatCapacityFitter {

    /**
     * Performs a linear least-squares fit for all defined HeatCapacityModels.
     *
     * @param temperatures   Array of temperature values (T)
     * @param heatCapacities Array of measured heat capacity values (C)
     * @return A map linking each physical model to its fitted coefficients.
     */
    public static Map<LowTHeatCapacityModel, FitResult> lowTPolyFit(double[] temperatures, double[] heatCapacities) {
        int nPoints = temperatures.length;

        Map<LowTHeatCapacityModel, FitResult> results = new EnumMap<>(LowTHeatCapacityModel.class);

        if (nPoints != heatCapacities.length) {
            throw new IllegalArgumentException("temperatures and heatCapacities arrays must have same length");
        }
        for (double heatCapacity : heatCapacities) {
            if (heatCapacity <= 0.0) {
                throw new IllegalArgumentException("heatCapacities must be positive");
            }
        }

        for (LowTHeatCapacityModel model : LowTHeatCapacityModel.values()) {
            double[] powers = model.getPowers();
            int numParams = powers.length;

            double[][] designMatrix = new double[nPoints][numParams];

            for (int point = 0; point < nPoints; point++) {
                for (int powerIdx = 0; powerIdx < numParams; powerIdx++) {
                    designMatrix[point][powerIdx] = Math.pow(temperatures[point], powers[powerIdx]) / heatCapacities[point];
                }
            }

            double[][] transposed = MatrixMath.transpose(designMatrix);
            double[][] ata = MatrixMath.multiply(transposed, designMatrix);

            double[] y = new double[numParams];
            for (int i = 0; i < numParams; i++) {
                double sum = 0;
                for (int j = 0; j < nPoints; j++) {
                    sum += transposed[i][j];
                }
                y[i] = sum;
            }

            double[] coefficients = MatrixMath.solveLinearSystem(ata, y);

            double sumOfSquaredRelativeErrors = 0.0;
            for (int point = 0; point < nPoints; point++) {
                double calculatedC = 0.0;
                for (int powerIdx = 0; powerIdx < numParams; powerIdx++) {
                    calculatedC += coefficients[powerIdx] * Math.pow(temperatures[point], powers[powerIdx]);
                }

                double relativeError = (heatCapacities[point] - calculatedC) / heatCapacities[point];
                sumOfSquaredRelativeErrors += relativeError * relativeError;
            }

            double pctRMS = Math.sqrt(sumOfSquaredRelativeErrors / nPoints) * 100.0;

            results.put(model, new FitResult(coefficients, pctRMS));

        }

        return results;
    }

    public static void print(Map<? extends HeatCapacityModel, FitResult> fittedModels) {
        fittedModels.forEach((model, result) -> {
            System.out.println("==================================================");
            System.out.printf("MODEL: %s   [%%RMS Error: %.4f%%]%n", model.name(), result.pctRMS());
            System.out.println("--------------------------------------------------");

            double[] powers = model.getPowers();
            double[] coefficients = result.coefficients();

            for (int i = 0; i < powers.length; i++) {
                System.out.printf("  T^(%.0f) coefficient: %e%n", powers[i], coefficients[i]);
            }
            System.out.println();
        });
    }


}
