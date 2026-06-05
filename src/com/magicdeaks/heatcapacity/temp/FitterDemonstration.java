package com.magicdeaks.heatcapacity.temp;

import com.magicdeaks.heatcapacity.records.FitResult;
import com.magicdeaks.heatcapacity.records.HeatCapacityData;
import com.magicdeaks.heatcapacity.models.CompositeSpecificHeatModel;
import com.magicdeaks.heatcapacity.util.LmCurveFitter;

import java.util.Arrays;

public class FitterDemonstration {

    static void main() {
        // 1. Define the Composite Model
        // Total Parameters: 1 (Linear) + 1 (Lattice_3) + 3 (Schottky) = 5 parameters
        CompositeSpecificHeatModel model = new CompositeSpecificHeatModel(
                CompositeSpecificHeatModel.SpecialFitModel.LINEAR,
                CompositeSpecificHeatModel.SpecialFitModel.LATTICE_3,
                CompositeSpecificHeatModel.SpecialFitModel.SCHOTTKY
        );

        // 2. Define the "True" Parameters to simulate experimental data
        // [gamma, beta, n, g, theta]
        double trueGamma = 5.0;     // Linear coefficient
        double trueBeta  = 0.25;    // Lattice coefficient
        double trueN     = 1.2;     // Schottky amplitude
        double trueG     = 2.0;     // Schottky degeneracy
        double trueTheta = 15.0;    // Schottky energy splitting

        double[] trueParams = { trueGamma, trueBeta, trueN, trueG, trueTheta };

        // 3. Generate Synthetic Heat Capacity Data
        int numPoints = 50;
        double[] temperatures = new double[numPoints];

        // Populate temperatures from 1K to 50K
        for (int i = 0; i < numPoints; i++) {
            temperatures[i] = i + 1.0;
        }

        // Calculate the ideal heat capacities using our model
        double[] idealHeatCapacities = model.value(temperatures, trueParams);

        // (Optional: In a real test, you would add Gaussian noise here to
        // simulate experimental measurement error)

        HeatCapacityData expData = new HeatCapacityData(temperatures, idealHeatCapacities);

        // 4. Configure the Fitting Engine
        // We will intentionally provide bad initial guesses to test convergence
        double[] initialGuesses = {
                1.0,   // Guess for gamma (True: 5.0)
                0.05,  // Guess for beta  (True: 0.25)
                0.5,   // Guess for n     (True: 1.2)
                1.0,   // Guess for g     (True: 2.0)
                5.0    // Guess for theta (True: 15.0)
        };

        // Let's assume we know the exact degeneracy ratio (g = 2.0) from quantum mechanics,
        // so we will fix it. We will leave the other 4 parameters free to optimize.
        initialGuesses[3] = 2.0; // Set to the known fixed value
        boolean[] fixedParams = { false, false, false, true, false };

        int maxIter = 2000;
        int maxEvals = 2000;

        // 5. Execute the Fit
        System.out.println("Starting Levenberg-Marquardt Optimization...");
        System.out.println("Initial Guesses: " + Arrays.toString(initialGuesses));

        try {
            FitResult result = LmCurveFitter.fit(
                    model,
                    expData,
                    initialGuesses,
                    fixedParams,
                    null,
                    null,
                    maxIter,
                    maxEvals
            );

            double[] finalCoeffs = result.coefficients();

            System.out.println("\nOptimization Successful!");
            System.out.println("========================");
            System.out.printf("Gamma           : %8.4f\n", finalCoeffs[0]);
            System.out.printf("Beta            : %8.4f\n", finalCoeffs[1]);
            System.out.printf("n     (Schottky): %8.4f\n", finalCoeffs[2]);
            System.out.printf("g     (Schottky): %8.4f\n", finalCoeffs[3]);
            System.out.printf("Theta (Schottky): %8.4f\n", finalCoeffs[4]);
            System.out.println("------------------------");
            System.out.printf("Relative Error : %.4f%%\n", result.pctRMS());

        } catch (Exception e) {
            System.err.println("\nOptimization Failed: " + e.getMessage());
        }
    }
}
