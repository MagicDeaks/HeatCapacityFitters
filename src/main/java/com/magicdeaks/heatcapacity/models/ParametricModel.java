package com.magicdeaks.heatcapacity.models;

public interface ParametricModel {
    /**
     * Calculates the Y values for given X values and parameters.
     */
    double[] value(double[] x, double[] parameters);

    /**
     * Calculates the Jacobian matrix (partial derivatives with respect to each parameter).
     * Returns a matrix of size [x.length][parameters.length].
     */
    double[][] jacobian(double[] x, double[] parameters);
}
