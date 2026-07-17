package com.magicdeaks.heatcapacity.util;

import com.magicdeaks.heatcapacity.models.ParametricModel;
import com.magicdeaks.heatcapacity.records.FitResult;
import com.magicdeaks.heatcapacity.records.HeatCapacityData;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.util.Pair;

import static com.magicdeaks.heatcapacity.util.Deviations.calculateRMS;

public abstract class LmCurveFitter {

    public static FitResult fit(
            ParametricModel model,
            HeatCapacityData data,
            double[] initialParams,
            boolean[] fixedParams,
            double[] lowerBounds,
            double[] upperBounds,
            int maxIterations,
            int maxEvaluations) {

        double[] xData = data.temperatures();
        double[] yData = data.heatCapacities();

        // 1. Determine how many parameters are actually free
        int count = 0;
        for (boolean isFixed : fixedParams) {
            if (!isFixed) count++;
        }
        final int finalNumFreeParams = count;

        if (finalNumFreeParams == 0) {
            // 6. Calculate Root Mean Square Percentage Error (RMSPE)
            double rmspe = calculateRMS(model, initialParams, xData, yData);

            return new FitResult(initialParams, rmspe);
        }

        double[] freeInitialParams = new double[finalNumFreeParams];
        int freeIdx = 0;
        for (int i = 0; i < initialParams.length; i++) {
            if (!fixedParams[i]) {
                freeInitialParams[freeIdx++] = initialParams[i];
            }
        }

        // 1b. Map the bounds to the free parameters
        double[] freeLowerBounds = new double[finalNumFreeParams];
        double[] freeUpperBounds = new double[finalNumFreeParams];
        int boundsIdx = 0;

        for (int i = 0; i < initialParams.length; i++) {
            if (!fixedParams[i]) {
                // Default to infinity if no bounds are provided
                freeLowerBounds[boundsIdx] = (lowerBounds != null) ? lowerBounds[i] : Double.NEGATIVE_INFINITY;
                freeUpperBounds[boundsIdx] = (upperBounds != null) ? upperBounds[i] : Double.POSITIVE_INFINITY;
                boundsIdx++;
            }
        }

        // 2. Create the Jacobian Function wrapper
        MultivariateJacobianFunction functionWrapper = freeParamsVector -> {

            double[] currentFreeParams = freeParamsVector.toArray();
            double[] allParams = new double[initialParams.length];

            // Reconstruct full parameter array
            int fIdx = 0;
            for (int i = 0; i < allParams.length; i++) {
                if (fixedParams[i]) {
                    allParams[i] = initialParams[i];
                } else {
                    allParams[i] = currentFreeParams[fIdx++];
                }
            }

            // Evaluate model
            double[] yValues = model.value(xData, allParams);
            double[][] fullJacobian = model.jacobian(xData, allParams);

            // Filter Jacobian columns to include only free parameters
            double[][] filteredJacobian = new double[xData.length][finalNumFreeParams];
            for (int i = 0; i < xData.length; i++) {
                fIdx = 0;
                for (int j = 0; j < allParams.length; j++) {
                    if (!fixedParams[j]) {
                        filteredJacobian[i][fIdx++] = fullJacobian[i][j];
                    }
                }
            }

            return new Pair<>(new ArrayRealVector(yValues), new Array2DRowRealMatrix(filteredJacobian));
        };

        // DEBUG: Manually check the Jacobian before handing it to the optimizer
        RealVector initialGuessVector = new ArrayRealVector(freeInitialParams);

        try {
            Pair<RealVector, RealMatrix> result = functionWrapper.value(initialGuessVector);
            System.out.println("DEBUG: Jacobian evaluation successful.");
            System.out.println("DEBUG: Jacobian shape: " + result.getSecond().getRowDimension() +
                    "x" + result.getSecond().getColumnDimension());
        } catch (Exception e) {
            System.err.println("DEBUG: Jacobian evaluation FAILED!");
        }

        // Create a weight matrix to minimize relative error
        double[] weights = new double[yData.length];
        for (int i = 0; i < yData.length; i++) {
            // Prevent division by zero if a data point is exactly 0
            weights[i] = yData[i] == 0 ? 1.0 : 1.0 / (yData[i] * yData[i]);
        }
        RealMatrix weightMatrix = new DiagonalMatrix(weights);

        // 3. Configure the Least Squares Problem
        LeastSquaresBuilder builder = new LeastSquaresBuilder()
                .model(functionWrapper)
                .target(yData)
                .start(freeInitialParams)
                .weight(weightMatrix)
                .maxIterations(maxIterations)
                .maxEvaluations(maxEvaluations);

        // Add the Parameter Validator to enforce bounds
        builder.parameterValidator(params -> {
            double[] currentParams = params.toArray();
            for (int i = 0; i < currentParams.length; i++) {
                if (currentParams[i] < freeLowerBounds[i]) {
                    currentParams[i] = freeLowerBounds[i];
                } else if (currentParams[i] > freeUpperBounds[i]) {
                    currentParams[i] = freeUpperBounds[i];
                }
            }
            return new ArrayRealVector(currentParams);
        });

        LeastSquaresProblem problem = builder.build();

        // 4. Run the Optimizer
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        Optimum optimum = optimizer.optimize(problem);

        // 5. Reconstruct the final parameters (now named coefficients)
        int iterations = optimum.getIterations();
        double[] optimizedFreeParams = optimum.getPoint().toArray();
        double[] coefficients = new double[initialParams.length];

        int freeIdxFinal = 0;
        for (int i = 0; i < initialParams.length; i++) {
            if (fixedParams[i]) {
                coefficients[i] = initialParams[i];
            } else {
                coefficients[i] = optimizedFreeParams[freeIdxFinal++];
            }
        }

        // 6. Calculate Root Mean Square Percentage Error (RMSPE)
        double rmspe = calculateRMS(model, coefficients, xData, yData);


        // 7. Return the updated record
        return new FitResult(coefficients, rmspe, iterations);
    }
}
