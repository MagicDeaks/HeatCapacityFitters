package com.magicdeaks.heatcapacity.util;

import com.magicdeaks.heatcapacity.models.ParametricModel;
import com.magicdeaks.heatcapacity.records.HeatCapacityData;

import java.util.stream.IntStream;

public class Deviations {
    private final double[] TEMPERATURES;
    private final double[] DEVIATIONS;

    public Deviations(double[][] data) {
        this.TEMPERATURES = data[0];
        this.DEVIATIONS = data[1];
    }

    public double[] getTemperatures() {
        return TEMPERATURES;
    }

    public double[] getDeviations() {
        return DEVIATIONS;
    }

    public static double[][] exportDeviations(
            Deviations lowDev,
            Deviations midDev,
            Deviations highDev,
            double lowOverlap,
            double highOverlap) {
        int minIdx =
                IntStream.range(0, highDev.getTemperatures().length)
                        .filter(i -> highDev.getTemperatures()[i] >= highOverlap)
                        .findFirst()
                        .orElse(0);
        int maxIdx =
                IntStream.range(0, lowDev.getTemperatures().length)
                        .filter(i -> lowDev.getTemperatures()[i] <= lowOverlap)
                        .reduce((_, second) -> second)
                        .orElse(lowDev.getTemperatures().length - 1);
        int midMinIdx =
                IntStream.range(0, midDev.getTemperatures().length)
                        .filter(i -> midDev.getTemperatures()[i] >= lowOverlap)
                        .findFirst()
                        .orElse(0);
        int midMaxIdx =
                IntStream.range(0, midDev.getTemperatures().length)
                        .filter(i -> midDev.getTemperatures()[i] <= highOverlap)
                        .reduce((_, second) -> second)
                        .orElse(lowDev.getTemperatures().length - 1);

        if (highDev.getTemperatures()[minIdx] == highOverlap) {
            minIdx += 1;
        }
        if (midDev.getTemperatures()[midMinIdx] == lowOverlap) {
            midMinIdx += 1;
        }

        double[] temperatures =
                new double
                        [maxIdx
                                + (1 + midMaxIdx - midMinIdx)
                                + (1 + highDev.getTemperatures().length - minIdx)
                                + 1];
        double[] deviations = new double[temperatures.length];

        for (int i = 0; i < temperatures.length; i++) {
            if (i <= maxIdx) {
                temperatures[i] = lowDev.getTemperatures()[i];
                deviations[i] = lowDev.getDeviations()[i];
            } else if ((i - (maxIdx + 1) + midMinIdx) <= midMaxIdx) {
                temperatures[i] = midDev.getTemperatures()[i - (maxIdx + 1) + midMinIdx];
                deviations[i] = highDev.getDeviations()[i - (maxIdx + 1) + midMinIdx];
            } else {
                temperatures[i] =
                        highDev.getTemperatures()[
                                i - (maxIdx + 1) - (midMaxIdx - midMinIdx + 1) + minIdx];
                deviations[i] =
                        highDev.getDeviations()[
                                i - (maxIdx + 1) - (midMaxIdx - midMinIdx + 1) + minIdx];
            }
        }

        return new double[][] {temperatures, deviations};
    }

    public static double calculateRMS(
            ParametricModel model, double[] params, double[] xData, double[] yData) {
        double[] fittedY = model.value(xData, params);
        double sumSquaredPercentageErrors = 0.0;

        for (int i = 0; i < yData.length; i++) {
            if (yData[i] != 0) {
                double percentageError = (yData[i] - fittedY[i]) / fittedY[i];
                sumSquaredPercentageErrors += (percentageError * percentageError);
            }
        }

        return Math.sqrt(sumSquaredPercentageErrors / yData.length) * 100;
    }

    public static double calculateRMS(
            double[] powers, double[] params, double[] xData, double[] yData) {
        double sumSquaredPercentageErrors = 0;
        for (int i = 0; i < yData.length; i++) {
            if (yData[i] != 0) {
                double fittedY = calculateHeatCapacity(params, powers, xData[i]);
                double percentageError = (yData[i] - fittedY) / fittedY;
                sumSquaredPercentageErrors += (percentageError * percentageError);
            }
        }

        return Math.sqrt(sumSquaredPercentageErrors / yData.length) * 100;
    }

    private static double calculateHeatCapacity(
            double[] coeff, double[] powers, double temperature) {
        double heatCapacity = 0;

        if (coeff.length != powers.length) {
            throw new IllegalArgumentException("coeff and powers arrays must have same length");
        }

        for (int i = 0; i < powers.length; i++) {
            heatCapacity += coeff[i] * Math.pow(temperature, powers[i]);
        }

        return heatCapacity;
    }

    public static double[][] getDeviations(
            ParametricModel model, double[] params, HeatCapacityData data) {
        double[] fittedY = model.value(data.temperatures(), params);
        double[] deviations = new double[fittedY.length];

        for (int i = 0; i < deviations.length; i++) {
            deviations[i] = (fittedY[i] - data.heatCapacities()[i]) / fittedY[i];
        }

        return new double[][] {data.temperatures(), deviations};
    }

    public static double[][] getDeviations(int[] powers, double[] params, HeatCapacityData data) {
        double[] fittedY = new double[data.temperatures().length];

        for (int i = 0; i < data.temperatures().length; i++) {
            for (int j = 0; j < powers.length; j++) {
                fittedY[i] += params[j] * Math.pow(data.temperatures()[i], powers[j]);
            }
        }

        double[] deviations = new double[fittedY.length];

        for (int i = 0; i < deviations.length; i++) {
            deviations[i] = (fittedY[i] - data.heatCapacities()[i]) / fittedY[i];
        }

        return new double[][] {data.temperatures(), deviations};
    }
}
