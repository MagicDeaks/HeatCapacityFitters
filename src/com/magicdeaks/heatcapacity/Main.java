package com.magicdeaks.heatcapacity;

import com.magicdeaks.heatcapacity.frames.MainFrame;
import com.magicdeaks.heatcapacity.models.LowTHeatCapacityModel;
import com.magicdeaks.heatcapacity.util.DataReader;

import javax.swing.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

public class Main {
    static void main() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainFrame mainFrame = new MainFrame();
                mainFrame.show();
            }
        });


        double[][] data = DataReader.readCSV("data.csv", new int[]{0, 1}, 0);
        Scanner scanner = new Scanner(System.in);
        double[] lowTUpperBound = {0};

        do {
            try {
                System.out.print("Low T Upper Bound: ");
                lowTUpperBound[0] = Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number...");
            }
        } while (lowTUpperBound[0] == 0);

        double[] lowTemperatures = Arrays.stream(data[0])
                .filter(val -> val < lowTUpperBound[0])
                .toArray();
        double[] lowHeatCapacities = Arrays.copyOf(data[1], lowTemperatures.length);

        long startTime = System.nanoTime();

        Map<LowTHeatCapacityModel, FitResult> fittedLowT = HeatCapacityFitter.lowTPolyFit(lowTemperatures, lowHeatCapacities);
        double[] orthoLowT = HeatCapacityFitter.fitOrthogonalPolynomial(lowTemperatures, lowHeatCapacities, 0, 1, 9);

        long endTime = System.nanoTime();

        HeatCapacityFitter.print(fittedLowT);
        System.out.println(Arrays.toString(orthoLowT));

        System.out.println("Execution time: " + ((endTime - startTime) / 1000000.0) + " ms");


    }
}