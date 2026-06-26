package com.magicdeaks.heatcapacity.util;

import com.magicdeaks.heatcapacity.models.ParametricModel;
import com.magicdeaks.heatcapacity.records.HeatCapacityData;

public abstract class Deviations {
    public static double calculateRMS(ParametricModel model, double[] params, double[] xData, double[] yData) {
        double[] fittedY = model.value(xData, params);
        double sumSquaredPercentageErrors = 0.0;

        for (int i = 0; i < yData.length; i++) {
            if (yData[i] != 0) {
                double percentageError = (yData[i] - fittedY[i]) / fittedY[i];
                sumSquaredPercentageErrors += (percentageError * percentageError);
            }
        }

        return Math.sqrt(sumSquaredPercentageErrors / yData.length) * 100.0;
    }

    public static double calculateRMS(double[] powers, double[] params, double[] xData, double[] yData) {
        double sumSquaredPercentageErrors = 0;
        for (int i = 0; i < yData.length; i++) {
            if (yData[i] != 0) {
                double fittedY = calculateHeatCapacity(params, powers, xData[i]);
                double percentageError = (yData[i] - fittedY) / fittedY;
                sumSquaredPercentageErrors += (percentageError * percentageError);
            }
        }

        return Math.sqrt(sumSquaredPercentageErrors / yData.length) * 100.0;
    }

    private static double calculateHeatCapacity(double[] coeff, double[] powers, double temperature) {
        double heatCapacity = 0;

        if (coeff.length != powers.length) {
            throw new IllegalArgumentException("coeff and powers arrays must have same length");
        }

        for (int i = 0; i < powers.length; i++) {
            heatCapacity += coeff[i] * Math.pow(temperature, powers[i]);
        }

        return heatCapacity;
    }

    public static double[][] getDeviations(ParametricModel model, double[] params, HeatCapacityData data) {
        double[] fittedY = model.value(data.temperatures(), params);
        double[] deviations = new double[fittedY.length];

        for (int i = 0; i < deviations.length; i++) {
            deviations[i] = (fittedY[i] - data.heatCapacities()[i]) / fittedY[i];
        }

        return new double[][]{ data.temperatures(), deviations };
    }
}
