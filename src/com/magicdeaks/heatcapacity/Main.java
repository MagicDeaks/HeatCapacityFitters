package com.magicdeaks.heatcapacity;

import com.magicdeaks.heatcapacity.models.CompositeSpecificHeatModel;
import com.magicdeaks.heatcapacity.records.FitResult;
import com.magicdeaks.heatcapacity.records.HeatCapacityData;
import com.magicdeaks.heatcapacity.util.DataProcesser;
import com.magicdeaks.heatcapacity.util.DataReader;
import com.magicdeaks.heatcapacity.util.LmCurveFitter;

import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

import static java.lang.System.nanoTime;

public class Main {
    static void main() {
//        SwingUtilities.invokeLater(() -> {
//            MainFrame mainFrame = new MainFrame();
//            mainFrame.show();
//        });

        CompositeSpecificHeatModel model = new CompositeSpecificHeatModel(
                CompositeSpecificHeatModel.SpecialFitModel.LINEAR,
                CompositeSpecificHeatModel.SpecialFitModel.LATTICE_3,
                CompositeSpecificHeatModel.SpecialFitModel.LATTICE_5,
                CompositeSpecificHeatModel.SpecialFitModel.LATTICE_7,
                CompositeSpecificHeatModel.SpecialFitModel.GAP
        );

        HeatCapacityData data = DataProcesser.scaleHeatCapacity(DataProcesser.subtractCopper(DataReader.readDAT("data.dat"), 14.42), 9.38, 0.667 + DataProcesser.getMolecularWeight("SmOHCO3"));

        int idx = IntStream.range(0, data.temperatures().length)
                .filter(i -> data.temperatures()[i] < 15.0)
                .boxed()
                .max(Comparator.comparingDouble(i -> data.temperatures()[i]))
                .orElse(-1);

        double[] lowT = Arrays.copyOfRange(data.temperatures(), 0, idx+1);
        double[] lowHC = Arrays.copyOfRange(data.heatCapacities(), 0, idx+1);

        System.out.println("Temperature Data: " + Arrays.toString(lowT));
        System.out.println("Heat Capacity Data: " + Arrays.toString(lowHC));

        HeatCapacityData lowData = new HeatCapacityData(lowT, lowHC);


        double[] initialParams = {0.000641, 0.000363, 0.000000713, -0.0000000015, 0.15, 3, 1.29};
        boolean[] fixedParams = {false, false, false, false, false, true, false};
        double[] lowerBounds = {
                0.0,
                0.0,
                Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY,
                0.0,
                0.0,
                1e-6
        };
        double[] upperBounds = null;

        long start = nanoTime();

        FitResult result = LmCurveFitter.fit(model, lowData, initialParams, fixedParams, lowerBounds, upperBounds, 500, 1500);

        long end = nanoTime();

        double[] finalCoeffs = result.coefficients();

        System.out.println("\nOptimization Successful!");
        System.out.println("Time elapsed: " + (end - start) / 1000000 + "ms");
        System.out.println("Iterations: " + result.iterations());
        System.out.println("========================");
        System.out.printf("Gamma           : %8.4e\n", finalCoeffs[0]);
        System.out.printf("Beta3           : %8.4e\n", finalCoeffs[1]);
        System.out.printf("Beta5           : %8.4e\n", finalCoeffs[2]);
        System.out.printf("Beta7           : %8.4e\n", finalCoeffs[3]);
        System.out.printf("n     (Schottky): %8.4e\n", finalCoeffs[4]);
        System.out.printf("g     (Schottky): %8.4e\n", finalCoeffs[5]);
        System.out.printf("Theta (Schottky): %8.4e\n", finalCoeffs[6]);
        System.out.println("------------------------");
        System.out.printf("Relative Error : %.4f%%\n", result.pctRMS());
    }
}