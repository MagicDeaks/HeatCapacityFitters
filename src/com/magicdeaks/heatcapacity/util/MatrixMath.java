package com.magicdeaks.heatcapacity.util;

public abstract class MatrixMath {
    private static final double EPSILON = 1e-9;

    public static double[][] transpose(double[][] matrix) {
        if (matrix.length == 0) return matrix;

        double[][] transposed = new double[matrix[0].length][matrix.length];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                transposed[j][i] = matrix[i][j]; // Swap row and column
            }
        }


        return transposed;
    }

    public static double[][] multiply(double[][] matrixA, double[][] matrixB) {
        int rowA = matrixA.length;
        int colA = matrixA[0].length;
        int rowB = matrixB.length;
        int colB = matrixB[0].length;

        if (colA != rowB) {
            throw new IllegalArgumentException("Matrix arrays must have same length");
        }

        double[][] result = new double[rowA][colB];

        for (int i = 0; i < rowA; i++) {
            for (int j = 0; j < colB; j++) {
                for (int k = 0; k < colA; k++) {
                    result[i][j] += matrixA[i][k] * matrixB[k][j];
                }
            }
        }

        return result;
    }

    /**
     * Computes the inverse of a square 2D matrix.
     *
     * @param matrix The input n x n double array.
     * @return The inverted matrix, or null if it is singular/non-square.
     */
    public static double[][] inverse(double[][] matrix) {
        // Validate square dimensions
        if (matrix == null || matrix.length == 0 || matrix.length != matrix[0].length) {
            throw new IllegalArgumentException("Matrix array must be square");
        }

        int n = matrix.length;

        // Step 1: Initialize augmented matrix [A | I]
        double[][] augmented = new double[n][2 * n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(matrix[i], 0, augmented[i], 0, n);
            augmented[i][i + n] = 1.0;
        }

        // Step 2: Perform Gauss-Jordan Elimination
        for (int i = 0; i < n; i++) {
            // Find pivot row (Partial Pivoting)
            int pivotRow = i;
            for (int j = i + 1; j < n; j++) {
                if (Math.abs(augmented[j][i]) > Math.abs(augmented[pivotRow][i])) {
                    pivotRow = j;
                }
            }

            // Swap current row with pivot row
            if (pivotRow != i) {
                double[] temp = augmented[i];
                augmented[i] = augmented[pivotRow];
                augmented[pivotRow] = temp;
            }

            // Check if the matrix is singular (determinant near 0)
            if (Math.abs(augmented[i][i]) < EPSILON) {
                throw new IllegalArgumentException("Matrix determinant cannot be zero");
            }

            // Scale pivot row to 1
            double pivotValue = augmented[i][i];
            for (int j = i; j < 2 * n; j++) {
                augmented[i][j] /= pivotValue;
            }

            // Eliminate columns across all other rows
            for (int k = 0; k < n; k++) {
                if (k != i) {
                    double factor = augmented[k][i];
                    for (int j = i; j < 2 * n; j++) {
                        augmented[k][j] -= factor * augmented[i][j];
                    }
                }
            }
        }

        // Step 3: Extract the inverted matrix [I | A^-1]
        double[][] inversed = new double[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(augmented[i], n, inversed[i], 0, n);
        }

        return inversed;
    }

    /**
     * Solves the linear system Ax = b using Gaussian elimination with partial pivoting.
     *
     * @param A An N x N matrix
     * @param b A vector of length N
     * @return The solution vector x
     */
    public static double[] solveLinearSystem(double[][] A, double[] b) {
        int n = b.length;

        // Create a copy of A and b to avoid mutating the original arrays
        double[][] matrix = new double[n][n];
        double[] vector = new double[n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, matrix[i], 0, n);
            vector[i] = b[i];
        }

        for (int p = 0; p < n; p++) {
            // Partial Pivoting: Find the row with the largest pivot element
            int max = p;
            for (int i = p + 1; i < n; i++) {
                if (Math.abs(matrix[i][p]) > Math.abs(matrix[max][p])) {
                    max = i;
                }
            }

            // Swap rows in the matrix
            double[] tempRow = matrix[p];
            matrix[p] = matrix[max];
            matrix[max] = tempRow;

            // Swap corresponding values in the vector
            double t = vector[p];
            vector[p] = vector[max];
            vector[max] = t;

            // Check for singular matrix
            if (Math.abs(matrix[p][p]) <= 1e-10) {
                throw new ArithmeticException("Matrix is singular or nearly singular");
            }

            // Pivot within A and b
            for (int i = p + 1; i < n; i++) {
                double alpha = matrix[i][p] / matrix[p][p];
                vector[i] -= alpha * vector[p];
                for (int j = p; j < n; j++) {
                    matrix[i][j] -= alpha * matrix[p][j];
                }
            }
        }

        // Back substitution to find the solution vector
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = 0.0;
            for (int j = i + 1; j < n; j++) {
                sum += matrix[i][j] * x[j];
            }
            x[i] = (vector[i] - sum) / matrix[i][i];
        }
        return x;
    }
}
