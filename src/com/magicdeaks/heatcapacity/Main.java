package com.magicdeaks.heatcapacity;

import com.magicdeaks.heatcapacity.frames.MainFrame;
import com.magicdeaks.heatcapacity.models.CompositeSpecificHeatModel;
import com.magicdeaks.heatcapacity.models.HighTSpecificHeatModel;
import com.magicdeaks.heatcapacity.records.FitResult;
import com.magicdeaks.heatcapacity.records.HeatCapacityData;
import com.magicdeaks.heatcapacity.util.DataProcesser;
import com.magicdeaks.heatcapacity.util.DataReader;
import com.magicdeaks.heatcapacity.util.LmCurveFitter;

import javax.swing.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

import static java.lang.System.nanoTime;

public class Main {
    static void main() {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.show();
        });

       // CompositeSpecificHeatModel lowTModel = new CompositeSpecificHeatModel(
       //         CompositeSpecificHeatModel.SpecialFitModel.LINEAR,
       //         CompositeSpecificHeatModel.SpecialFitModel.LATTICE_3,
       //         CompositeSpecificHeatModel.SpecialFitModel.LATTICE_5,
       //         CompositeSpecificHeatModel.SpecialFitModel.LATTICE_7,
       //         CompositeSpecificHeatModel.SpecialFitModel.SCHOTTKY
       // );

       // HighTSpecificHeatModel highTModel = new HighTSpecificHeatModel(
       //         HighTSpecificHeatModel.HighTFitModel.DEBYE,
       //         HighTSpecificHeatModel.HighTFitModel.EINSTEIN,
       //         HighTSpecificHeatModel.HighTFitModel.EINSTEIN
       // );

       // HeatCapacityData data = DataProcesser.scaleHeatCapacity(DataProcesser.subtractCopper(DataReader.readDAT("data.dat"), 14.42), 9.38, 0.667 + DataProcesser.getMolecularWeight("SmOHCO3"));

       // int lowIdx = IntStream.range(0, data.temperatures().length)
       //         .filter(i -> data.temperatures()[i] < 15.0)
       //         .boxed()
       //         .max(Comparator.comparingDouble(i -> data.temperatures()[i]))
       //         .orElse(-1);

       // int highIdx = IntStream.range(0, data.temperatures().length)
       //         .filter(i -> data.temperatures()[i] > 40.0)
       //         .boxed()
       //         .min(Comparator.comparingDouble(i -> data.temperatures()[i]))
       //         .orElse(-1);

       // double[] lowT = Arrays.copyOfRange(data.temperatures(), 0, lowIdx+1);
       // double[] lowHC = Arrays.copyOfRange(data.heatCapacities(), 0, lowIdx+1);

       // double[] highT = Arrays.copyOfRange(data.temperatures(), highIdx, data.temperatures().length);
       // double[] highHC = Arrays.copyOfRange(data.heatCapacities(), highIdx, data.heatCapacities().length);

       // System.out.println("Low Temperature Data: " + Arrays.toString(lowT));
       // System.out.println("Low Heat Capacity Data: " + Arrays.toString(lowHC));

       // System.out.println("High Temperature Data: " + Arrays.toString(highT));
       // System.out.println("High Heat Capacity Data: " + Arrays.toString(highHC));

       // HeatCapacityData lowData = new HeatCapacityData(lowT, lowHC);
       // HeatCapacityData highData = new HeatCapacityData(highT, highHC);


       // double[] lowInitParams = {0.000641, 0.000363, 0.000000713, -0.0000000015, 0.15, 1, 1.29};
       // boolean[] lowFixedParams = {false, false, false, false, false, true, false};
       // double[] lowLowerBounds = {
       //         0.0,
       //         0.0,
       //         Double.NEGATIVE_INFINITY,
       //         Double.NEGATIVE_INFINITY,
       //         0.0,
       //         0.0,
       //         1e-6
       // };
       // double[] lowUpperBounds = null;

       // double[] highInitParams = {2, 150, 2, 300, 3, 900};
       // boolean[] highFixedParams = {false, false, false, false, false, false};
       // double[] highLowerBounds = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
       // double[] highUpperBounds = null;

       // long start = nanoTime();

       // FitResult lowResult = LmCurveFitter.fit(lowTModel, lowData, lowInitParams, lowFixedParams, lowLowerBounds, lowUpperBounds, 500, 1500);
       // FitResult highResult = LmCurveFitter.fit(highTModel, highData, highInitParams, highFixedParams, highLowerBounds, highUpperBounds, 15000, 45000);

       // long end = nanoTime();

       // double[] lowFinalCoeffs = lowResult.coefficients();
       // double[] highFinalCoeffs = highResult.coefficients();


       // System.out.println("Time elapsed: " + (end - start) / 1000000 + "ms");

       // System.out.println("\nLow T Optimization Successful!");
       // System.out.println("Iterations: " + lowResult.iterations());
       // System.out.println("========================");
       // System.out.printf("Gamma           : %8.4e\n", lowFinalCoeffs[0]);
       // System.out.printf("Beta3           : %8.4e\n", lowFinalCoeffs[1]);
       // System.out.printf("Beta5           : %8.4e\n", lowFinalCoeffs[2]);
       // System.out.printf("Beta7           : %8.4e\n", lowFinalCoeffs[3]);
       // System.out.printf("n     (Schottky): %8.4e\n", lowFinalCoeffs[4]);
       // System.out.printf("g     (Schottky): %8.4e\n", lowFinalCoeffs[5]);
       // System.out.printf("Theta (Schottky): %8.4e\n", lowFinalCoeffs[6]);
       // System.out.println("------------------------");
       // System.out.printf("Relative Error : %.4f%%\n", lowResult.pctRMS());

       // System.out.println("\nHigh T Optimization Successful!");
       // System.out.println("Iterations: " + highResult.iterations());
       // System.out.println("========================");
       // System.out.printf("n                    : %8.4e\n", highFinalCoeffs[0]);
       // System.out.printf("Theta(D)             : %8.4e\n", highFinalCoeffs[1]);
       // System.out.printf("m1                   : %8.4e\n", highFinalCoeffs[2]);
       // System.out.printf("Theta(E)1            : %8.4e\n", highFinalCoeffs[3]);
       // System.out.printf("m2                   : %8.4e\n", highFinalCoeffs[4]);
       // System.out.printf("Theta(E)2            : %8.4e\n", highFinalCoeffs[5]);
       // System.out.println("------------------------");
       // System.out.printf("Relative Error : %.4f%%\n", highResult.pctRMS());
    }
}