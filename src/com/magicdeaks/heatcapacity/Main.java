package com.magicdeaks.heatcapacity;

import java.util.Map;

public class Main {
    static void main() {
        double[][] data = DataReader.readCSV("data.csv");

        double[] temperatures = data[0];
        double[] heatCapacities = data[1];

        long startTime = System.nanoTime();

        Map<LowTHeatCapacityModel, FitResult> fittedLowT = HeatCapacityFitter.lowTPolyFit(temperatures, heatCapacities);

        long endTime = System.nanoTime();

        HeatCapacityFitter.print(fittedLowT);

        System.out.println("Execution time: " + ((endTime - startTime) / 1000000.0) + " ms");
    }
}