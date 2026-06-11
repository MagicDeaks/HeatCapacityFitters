package com.magicdeaks.heatcapacity.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompositeSpecificHeatModel implements ParametricModel {

    public enum SpecialFitModel {
        LINEAR, LATTICE_3, LATTICE_5, LATTICE_7, LATTICE_9, LATTICE_11, LATTICE_13,
        GAP, SCHOTTKY, UPTURN_2, UPTURN_3, UPTURN_4
    }

    private final List<ModelTerm> TERMS = new ArrayList<>();
    private final int TOTAL_PARAMETERS;

    /**
     * Constructor that accepts a variable number of SpecialFitModel enums.
     * The order of enums dictates the order of parameters in the parameter array.
     */
    public CompositeSpecificHeatModel(SpecialFitModel... models) {
        int paramCount = 0;
        for (SpecialFitModel model : models) {
            ModelTerm term = createTerm(model, paramCount);
            TERMS.add(term);
            paramCount += term.getParameterCount();
        }
        this.TOTAL_PARAMETERS = paramCount;
    }

    public int getTotalParameters() {
        return TOTAL_PARAMETERS;
    }

    @Override
    public double[] value(double[] tData, double[] parameters) {
        if (parameters.length != TOTAL_PARAMETERS) {
            throw new IllegalArgumentException("Expected " + TOTAL_PARAMETERS + " parameters, got " + parameters.length);
        }

        double[] yValues = new double[tData.length];
        for (int i = 0; i < tData.length; i++) {
            double t = tData[i];
            double cTotal = 0.0;
            for (ModelTerm term : TERMS) {
                cTotal += term.evaluate(t, parameters);
            }
            yValues[i] = cTotal;
        }
        return yValues;
    }

    @Override
    public double[][] jacobian(double[] tData, double[] parameters) {
        double[][] jacobianMatrix = new double[tData.length][TOTAL_PARAMETERS];

        for (int i = 0; i < tData.length; i++) {
            double t = tData[i];
            for (ModelTerm term : TERMS) {
                term.addJacobianDerivatives(t, parameters, jacobianMatrix[i]);
            }
        }

        return jacobianMatrix;
    }

    // --- Internal Logic for Mathematical Mapping ---

    private abstract static class ModelTerm {
        protected final int paramOffset;

        public ModelTerm(int paramOffset) {
            this.paramOffset = paramOffset;
        }

        abstract int getParameterCount();
        abstract double evaluate(double t, double[] params);
        abstract void addJacobianDerivatives(double t, double[] params, double[] jacobianRow);
    }

    private ModelTerm createTerm(SpecialFitModel model, int offset) {
        return switch (model) {
            case LINEAR -> new PolynomialTerm(offset, 1);
            case LATTICE_3 -> new PolynomialTerm(offset, 3);
            case LATTICE_5 -> new PolynomialTerm(offset, 5);
            case LATTICE_7 -> new PolynomialTerm(offset, 7);
            case LATTICE_9 -> new PolynomialTerm(offset, 9);
            case LATTICE_11 -> new PolynomialTerm(offset, 11);
            case LATTICE_13 -> new PolynomialTerm(offset, 13);
            case UPTURN_2 -> new PolynomialTerm(offset, -2);
            case UPTURN_3 -> new PolynomialTerm(offset, -3);
            case UPTURN_4 -> new PolynomialTerm(offset, -4);
            case GAP -> new GapTerm(offset);
            case SCHOTTKY -> new SchottkyTerm(offset);
            default -> throw new IllegalArgumentException("Unknown model: " + model);
        };
    }

    // 1-Parameter Polynomial: C = p * T^power
    private static class PolynomialTerm extends ModelTerm {
        private final double POWER;

        public PolynomialTerm(int offset, double power) {
            super(offset);
            this.POWER = power;
        }

        @Override int getParameterCount() { return 1; }

        @Override
        double evaluate(double t, double[] params) {
            return params[paramOffset] * Math.pow(t, POWER);
        }

        @Override
        void addJacobianDerivatives(double t, double[] params, double[] jacobianRow) {
            jacobianRow[paramOffset] = Math.pow(t, POWER); // dC/dp
        }
    }

    // 3-Parameter Generalized Gap: C = B * T^n * exp(-delta / T)
    // params[offset]   = B (Scaling factor)
    // params[offset+1] = n (Power of T)
    // params[offset+2] = delta (Energy Gap)
    private static class GapTerm extends ModelTerm {
        public GapTerm(int offset) { super(offset); }

        @Override int getParameterCount() { return 3; }

        @Override
        double evaluate(double t, double[] params) {
            double b = params[paramOffset];
            double n = params[paramOffset + 1];
            double delta = params[paramOffset + 2];
            return b * Math.pow(t, n) * Math.exp(-delta / t);
        }

        @Override
        void addJacobianDerivatives(double t, double[] params, double[] jacobianRow) {
            double b = params[paramOffset];
            double n = params[paramOffset + 1];
            double delta = params[paramOffset + 2];

            double tPowN = Math.pow(t, n);
            double expFactor = Math.exp(-delta / t);
            double baseTerm = b * tPowN * expFactor;

            // dC/dB
            jacobianRow[paramOffset] = tPowN * expFactor;

            // dC/dn
            jacobianRow[paramOffset + 1] = baseTerm * Math.log(t);

            // dC/dDelta
            jacobianRow[paramOffset + 2] = baseTerm * (-1.0 / t);
        }
    }

    // 3-Parameter Schottky: C = n * (theta/T)^2 * [g * exp(theta/T)] / [1 + g * exp(theta/T)]^2
    // params[offset]   = n (Scaling factor / Moles)
    // params[offset+1] = g (Degeneracy ratio)
    // params[offset+2] = theta (Energy splitting)
    private static class SchottkyTerm extends ModelTerm {
        public SchottkyTerm(int offset) { super(offset); }

        @Override int getParameterCount() { return 3; }

        @Override
        double evaluate(double t, double[] params) {
            double n = params[paramOffset];
            double g = params[paramOffset + 1];
            double theta = params[paramOffset + 2];

            if (t < 1e-9) return 0.0; // Guard against division by zero

            double x = theta / t;
            // Use exp(-x) to prevent overflow
            double expNegX = Math.exp(-x);

            double R = 8.314472;

            // C = n * R * x^2 * (g * exp(-x)) / (1 + g * exp(-x))^2)
            // Note: This is algebraically equivalent to your original
            return n * R * (x * x) * (g * expNegX) / Math.pow(1.0 + g * expNegX, 2);
        }

        @Override
        void addJacobianDerivatives(double t, double[] params, double[] jacobianRow) {
            double n = params[paramOffset];
            double g = params[paramOffset + 1];
            double theta = params[paramOffset + 2];

            if (t < 1e-9) {
                Arrays.fill(jacobianRow, 0.0);
                return;
            }

            double R = 8.314472;

            double x = theta / t;
            double expNegX = Math.exp(-x);
            double denom = 1.0 + g * expNegX;
            double gExpNegX = g * expNegX;

            // dC/dn
            jacobianRow[paramOffset] = (x * x) * gExpNegX / (denom * denom) * R;

            // dC/dg
            // Using the same stable logic
            jacobianRow[paramOffset + 1] = n * (x * x) * expNegX * (1.0 - gExpNegX) / Math.pow(denom, 3) * R;

            // dC/dTheta
            // Stable derivative w.r.t theta
            double term1 = (n * g * x * expNegX) / t;
            double term2 = (2.0 - x) + gExpNegX * (2.0 + x);
            jacobianRow[paramOffset + 2] = (term1 * term2) / Math.pow(denom, 3) * R;
        }
    }
}