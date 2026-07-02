package com.magicdeaks.heatcapacity.util;

import com.magicdeaks.heatcapacity.models.HeatCapacityModel;
import com.magicdeaks.heatcapacity.models.LowTHeatCapacityModel;
import com.magicdeaks.heatcapacity.records.FitResult;
import com.magicdeaks.heatcapacity.records.HeatCapacityData;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import static com.magicdeaks.heatcapacity.util.Deviations.calculateRMS;

public abstract class PolyCurveFitter {

    /**
     * Performs a linear least-squares fit for all defined HeatCapacityModels.
     *
     * @param data The data in the form of HeatCapacityData
     * @return A map linking each physical model to its fitted coefficients.
     */
    public static Map<LowTHeatCapacityModel, FitResult> lowTPolyFit(HeatCapacityData data) {
        double[] temperatures = data.temperatures();
        double[] heatCapacities = data.heatCapacities();

        int numPoints = temperatures.length;

        // Past me was getting a little too fancy. Current me doesn't know how to undo his mistakes
        Map<LowTHeatCapacityModel, FitResult> results = new EnumMap<>(LowTHeatCapacityModel.class);

        if (numPoints != heatCapacities.length) {
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

            double[][] designMatrix = new double[numPoints][numParams];

            for (int point = 0; point < numPoints; point++) {
                for (int powerIdx = 0; powerIdx < numParams; powerIdx++) {
                    designMatrix[point][powerIdx] = Math.pow(temperatures[point], powers[powerIdx]) / heatCapacities[point];
                }
            }

            double[][] transposed = MatrixMath.transpose(designMatrix);
            double[][] ata = MatrixMath.multiply(transposed, designMatrix);

            double[] y = new double[numParams];
            for (int i = 0; i < numParams; i++) {
                double sum = 0;
                for (int j = 0; j < numPoints; j++) {
                    sum += transposed[i][j];
                }
                y[i] = sum;
            }

            double[] coefficients = MatrixMath.solveLinearSystem(ata, y);

            double pctRMS = calculateRMS(powers, coefficients, temperatures, heatCapacities);

            results.put(model, new FitResult(coefficients, pctRMS));

        }

        return results;
    }

    // Don't even ask... Gemini wrote it, I am merely a vessel for the great AI overlords.
    // Status update: It doesn't work
    // Status update: It might work
    public static FitResult fitOrthogonalPolynomial(HeatCapacityData data, int startPower, int powerIncrement, int totalTerms) {
        double[] temperatures = data.temperatures();
        double[] heatCapacities = data.heatCapacities();

        int numberOfPoints = temperatures.length;

        if (numberOfPoints != heatCapacities.length) {
            throw new IllegalArgumentException("temperatures and heatCapacities arrays must have the same length.");
        }

        double maxTemperature = -Double.MAX_VALUE;
        for (int i = 0; i < numberOfPoints; i++) {
            if (heatCapacities[i] <= 0.0) {
                throw new IllegalArgumentException("heatCapacities must be positive.");
            }
            if (temperatures[i] > maxTemperature) maxTemperature = temperatures[i];
        }

        // FIX: Remove centering to avoid generating non-sparse cross-terms.
        // We still scale by maxTemperature to ensure numerical stability.
        double tempScaleFactor = maxTemperature; 

        double[] scaledTemperatures = new double[numberOfPoints];
        for (int i = 0; i < numberOfPoints; i++) {
            scaledTemperatures[i] = temperatures[i] / tempScaleFactor;
        }

        double[] currentOrthoPoly = new double[numberOfPoints];
        double[] prevOrthoPoly = new double[numberOfPoints];
        double[] prevPrevOrthoPoly = new double[numberOfPoints];
        double[] weightsSquared = new double[numberOfPoints];

        for (int i = 0; i < numberOfPoints; i++) {
            currentOrthoPoly[i] = Math.pow(scaledTemperatures[i], startPower);
            double weight = 1.0 / heatCapacities[i];
            weightsSquared[i] = weight * weight; // Standard relative error weighting
        }

        int maxIterations = totalTerms + 2;
        double[] prevPrevBasisCoeffs = new double[maxIterations];
        double[] prevBasisCoeffs = new double[maxIterations];
        double[] currentBasisCoeffs = new double[maxIterations];
        double[] orthoFitCoeffs = new double[maxIterations];

        double recurrenceAlpha = 0, recurrenceBeta = 0, prevNormSquared = 1.0;

        int currentIteration = 1;
        currentBasisCoeffs[0] = 1.0; 

        // FIX: Removed while(true) and fixed iteration targets
        while (currentIteration <= totalTerms) {
            double sumPolySq = 0;
            double sumXPolySq = 0;
            double sumXPolyPrevPoly = 0;
            double sumYPoly = 0;

            for (int i = 0; i < numberOfPoints; i++) {
                if (currentIteration != 1) {
                    currentOrthoPoly[i] = (Math.pow(scaledTemperatures[i], powerIncrement) - recurrenceAlpha) * prevOrthoPoly[i] - recurrenceBeta * prevPrevOrthoPoly[i];
                }

                double polySq = currentOrthoPoly[i] * currentOrthoPoly[i];
                double xIncr = Math.pow(scaledTemperatures[i], powerIncrement);

                sumPolySq += weightsSquared[i] * polySq;
                sumXPolySq += weightsSquared[i] * xIncr * polySq;
                sumXPolyPrevPoly += weightsSquared[i] * xIncr * currentOrthoPoly[i] * prevOrthoPoly[i];
                sumYPoly += weightsSquared[i] * currentOrthoPoly[i] * heatCapacities[i];

                prevPrevOrthoPoly[i] = prevOrthoPoly[i];
                prevOrthoPoly[i] = currentOrthoPoly[i];
            }

            recurrenceAlpha = sumXPolySq / sumPolySq;
            double currentFitStep = sumYPoly / sumPolySq;
            recurrenceBeta = sumXPolyPrevPoly / prevNormSquared;
            prevNormSquared = sumPolySq;

            for (int i = 0; i < currentIteration; i++) {
                orthoFitCoeffs[i] += currentFitStep * currentBasisCoeffs[i];
            }

            if (currentIteration == totalTerms) break;

            // FIX: Clean, mathematically direct calculation of the next basis coefficients
            double[] nextBasisCoeffs = new double[maxIterations];
            for (int i = 0; i <= currentIteration; i++) {
                double termX = (i > 0) ? currentBasisCoeffs[i - 1] : 0.0;
                double termAlpha = recurrenceAlpha * currentBasisCoeffs[i];
                double termBeta = recurrenceBeta * prevBasisCoeffs[i];
                nextBasisCoeffs[i] = termX - termAlpha - termBeta;
            }

            // Clean array shifting
            System.arraycopy(prevBasisCoeffs, 0, prevPrevBasisCoeffs, 0, maxIterations);
            System.arraycopy(currentBasisCoeffs, 0, prevBasisCoeffs, 0, maxIterations);
            System.arraycopy(nextBasisCoeffs, 0, currentBasisCoeffs, 0, maxIterations);

            currentIteration++;
        }

        // FIX: Removed tangled binomial expansion. 
        // Back-transformation is now a robust, direct inverse scale.
        double[] powers = new double[totalTerms];
        double[] finalStandardCoeffs = new double[totalTerms];

        for (int j = 0; j < totalTerms; j++) {
            powers[j] = startPower + powerIncrement * j;
            finalStandardCoeffs[j] = orthoFitCoeffs[j] / Math.pow(tempScaleFactor, powers[j]);
        }

        double pctRMS = calculateRMS(powers, finalStandardCoeffs, temperatures, heatCapacities);

        return new FitResult(finalStandardCoeffs, pctRMS);
    }
    /**
     * Calculates heat capacity values from a given fit
     *
     * @param data The experimental data, containing the temperatures to be calculated at
     * @param fit An array of pairs, with the first of each pair being the coefficient
     *            and the second being the power of T
     * @return An array of heat capacity values
     */
    public static double[] evaluatePolynomial(HeatCapacityData data, double[][] fit) {
        double[] result = new double[data.temperatures().length];

        for (int i = 0; i < data.temperatures().length; i++) {
            for (double[] pair : fit) {
                result[i] += pair[0] * Math.pow(data.temperatures()[i], pair[1]);
            }
        }

        return result;
    }

    public static double[] evaluatePolynomial(double[] data, double[][] fit) {
        double[] result = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            for (double[] pair : fit) {
                result[i] += pair[0] * Math.pow(data[i], pair[1]);
            }
        }

        return result;
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
