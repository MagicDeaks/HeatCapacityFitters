package com.magicdeaks.heatcapacity.util;

import com.magicdeaks.heatcapacity.records.FitResult;
import com.magicdeaks.heatcapacity.records.HeatCapacityData;
import com.magicdeaks.heatcapacity.models.HeatCapacityModel;
import com.magicdeaks.heatcapacity.models.LowTHeatCapacityModel;

import java.util.EnumMap;
import java.util.Map;

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

            double sumOfSquaredRelativeErrors = 0.0;
            for (int point = 0; point < numPoints; point++) {
                double calculatedC = 0.0;
                for (int powerIdx = 0; powerIdx < numParams; powerIdx++) {
                    calculatedC += coefficients[powerIdx] * Math.pow(temperatures[point], powers[powerIdx]);
                }

                double relativeError = (heatCapacities[point] - calculatedC) / heatCapacities[point];
                sumOfSquaredRelativeErrors += relativeError * relativeError;
            }

            double pctRMS = Math.sqrt(sumOfSquaredRelativeErrors / numPoints) * 100.0;

            results.put(model, new FitResult(coefficients, pctRMS));

        }

        return results;
    }

    public static double[] fitOrthogonalPolynomial(HeatCapacityData data, int startPower, int powerIncrement, int totalTerms) {
        double[] temperatures = data.temperatures();
        double[] heatCapacities = data.heatCapacities();

        int numberOfPoints = temperatures.length;

        if (numberOfPoints != heatCapacities.length) {
            throw new IllegalArgumentException("temperatures and heatCapacities arrays must have the same length.");
        }

        double minTemperature = Double.MAX_VALUE;
        double maxTemperature = -Double.MAX_VALUE;

        for (int i = 0; i < numberOfPoints; i++) {
            if (heatCapacities[i] <= 0.0) {
                throw new IllegalArgumentException("heatCapacities must be positive.");
            }
            if (temperatures[i] < minTemperature) minTemperature = temperatures[i];
            if (temperatures[i] > maxTemperature) maxTemperature = temperatures[i];
        }

        double[] currentOrthoPoly = new double[numberOfPoints];
        double[] prevOrthoPoly = new double[numberOfPoints];
        double[] prevPrevOrthoPoly = new double[numberOfPoints];
        double[] weightsSquared = new double[numberOfPoints];

        for (int i = 0; i < numberOfPoints; i++) {
            currentOrthoPoly[i] = Math.pow(temperatures[i], startPower);
            double weight = 1.0 / heatCapacities[i];
            weightsSquared[i] = weight * weight;
        }

        double tempMidpoint = minTemperature + (maxTemperature - minTemperature) / 2.0;
        double tempScaleFactor = maxTemperature - tempMidpoint;

        double[] scaledTemperatures = new double[numberOfPoints];
        for (int i = 0; i < numberOfPoints; i++) {
            scaledTemperatures[i] = (temperatures[i] - tempMidpoint) / tempScaleFactor;
        }

        int maxIterations = (int) Math.ceil((double) (totalTerms - startPower + powerIncrement) / powerIncrement) + 2;
        double[] prevPrevBasisCoeffs = new double[maxIterations];
        double[] prevBasisCoeffs = new double[maxIterations];
        double[] currentBasisCoeffs = new double[maxIterations];
        double[] orthoFitCoeffs = new double[maxIterations];

        double recurrenceAlpha = 0, recurrenceBeta = 0, prevNormSquared = 1.0;
        double targetIterations = (double) (totalTerms - startPower + powerIncrement) / powerIncrement;

        int currentIteration = 1;
        currentBasisCoeffs[0] = 1;

        // Orthogonal fitting iteration loop
        while (true) {
            double sumPolySq = 0;
            double sumXPolySq = 0;
            double sumXPolyPrevPoly = 0;
            double sumYPoly = 0;

            for (int i = 0; i < numberOfPoints; i++) {
                if (currentIteration != 1) {
                    currentOrthoPoly[i] = (Math.pow(temperatures[i], powerIncrement) - recurrenceAlpha) * prevOrthoPoly[i] - recurrenceBeta * prevPrevOrthoPoly[i];
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

            if (currentIteration >= targetIterations) break;

            int lastIterCount = currentIteration;
            currentIteration += 1;
            currentBasisCoeffs[currentIteration] = 1;

            for (int i = 0; i < lastIterCount; i++) {
                if (currentIteration - i != 0) {
                    prevPrevBasisCoeffs[currentIteration - i - 1] = prevBasisCoeffs[currentIteration - i - 1];
                }
                prevBasisCoeffs[currentIteration - i] = currentBasisCoeffs[currentIteration - i];
            }

            for (int i = 0; i < lastIterCount; i++) {
                currentBasisCoeffs[i] = 0;
                if (i != 0) {
                    currentBasisCoeffs[i] += prevBasisCoeffs[i - 1];
                }

                currentBasisCoeffs[i] -= recurrenceAlpha * prevBasisCoeffs[i];
                if (currentIteration >= 3 && currentIteration - 2 >= i) {
                    currentBasisCoeffs[i] -= recurrenceBeta * prevPrevBasisCoeffs[i];
                }
            }
        }

        // Matrix generation for back-transformation to standard polynomial coefficients
        double[][] binomialMatrix = new double[totalTerms + 1][totalTerms + 1];
        double[] finalStandardCoeffs = new double[totalTerms + 1];

        for (int i = 0; i < totalTerms + 1; i++) {
            binomialMatrix[0][i] = 1;
            binomialMatrix[i][0] = 1;
        }

        for (int i = 1; i < totalTerms + 1; i++) {
            for (int j = 1; j < totalTerms + 1; j++) {
                binomialMatrix[i][j] = binomialMatrix[i - 1][j] + binomialMatrix[i][j - 1];
            }
        }

        for (int i = 0; i < totalTerms + 1; i++) {
            int binomialOffset = 0;
            for (int j = i; j < totalTerms + 1; j++) {
                double sign = ((j + i) % 2 == 0) ? 1.0 : -1.0;
                finalStandardCoeffs[i] += (binomialMatrix[i][binomialOffset] * orthoFitCoeffs[j] * Math.pow(tempMidpoint, j - i) * sign) / Math.pow(tempScaleFactor, j);
                binomialOffset++;
            }
        }

        return finalStandardCoeffs;
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
