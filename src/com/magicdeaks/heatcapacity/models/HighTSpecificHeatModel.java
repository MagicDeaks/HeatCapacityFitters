package com.magicdeaks.heatcapacity.models;

import com.magicdeaks.heatcapacity.util.HighTFunctions;

import java.util.ArrayList;
import java.util.List;

public class HighTSpecificHeatModel implements ParametricModel{
    public static int intervals = 6;

    public enum HighTFitModel {
        LINEAR, SQUARE, DEBYE, EINSTEIN
    }

    private final List<ModelTerm> TERMS = new ArrayList<>();
    private final int TOTAL_PARAMETERS;

    public HighTSpecificHeatModel(HighTFitModel... models) {
        int paramCount = 0;
        for (HighTFitModel model : models) {
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

        double [] yValues = new double[tData.length];
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

    private abstract static class ModelTerm {
        protected final int paramOffset;

        public ModelTerm(int paramOffset) { this.paramOffset = paramOffset; }

        abstract int getParameterCount();
        abstract double evaluate(double t, double[] params);
        abstract void addJacobianDerivatives(double t, double[] params, double[] jacobianRow);
    }

    private ModelTerm createTerm(HighTFitModel model, int offset) {
        return switch (model) {
            case LINEAR -> new PolynomialTerm(offset, 1);
            case SQUARE -> new PolynomialTerm(offset, 2);
            case DEBYE -> new DebyeTerm(offset);
            case EINSTEIN -> new EinsteinTerm(offset);
            default -> throw new IllegalArgumentException("Unknown Model: " + model);
        };
    }

    private static class PolynomialTerm extends ModelTerm {
        private final double POWER;

        public PolynomialTerm(int offset, double power) {
            super(offset);
            this.POWER = power;
        }

        @Override int getParameterCount() { return 1; }

        @Override
        double evaluate(double t, double[] params) {return params[paramOffset] * Math.pow(t, POWER); }

        @Override
        void addJacobianDerivatives(double t, double[] params, double[] jacobianRow) {
            jacobianRow[paramOffset] = Math.pow(t, POWER);
        }
    }

    private static class DebyeTerm extends ModelTerm {
        public DebyeTerm(int offset) { super(offset); }

        @Override int getParameterCount() { return 2; }

        @Override
        double evaluate(double t, double[] params) {
            double n = params[paramOffset];
            double theta = params[paramOffset + 1];

            return n * HighTFunctions.calculateDebye(theta, t, intervals);
        }

        @Override
        void addJacobianDerivatives(double t, double[] params, double[] jacobianRow) {
            double n  = params[paramOffset];
            double theta = params[paramOffset + 1];

            double expMinus = Math.exp(-theta / t);

            jacobianRow[paramOffset] = HighTFunctions.calculateDebye(theta, t, intervals);

            jacobianRow[paramOffset + 1] = -3 / theta * jacobianRow[paramOffset];

            double intermediate = expMinus / ((1 - expMinus) * (1 - expMinus));
            jacobianRow[paramOffset + 1] += n * 9 * 8.314472 * theta / (t * t) * intermediate;
        }
    }

    private static class EinsteinTerm extends ModelTerm {
        public EinsteinTerm(int offset) { super(offset); }

        @Override int getParameterCount() { return 2; }

        @Override
        double evaluate(double t, double[] params) {
            double n = params[paramOffset];
            double theta = params[paramOffset + 1];

            return n * HighTFunctions.calculateEinstein(theta, t);
        }

        @Override
        void addJacobianDerivatives(double t, double[] params, double[] jacobianRow) {
            double n = params[paramOffset];
            double theta = params[paramOffset + 1];
            double expMinus = Math.exp(-theta / t);

            jacobianRow[paramOffset] = HighTFunctions.calculateEinstein(theta, t);

            double intermediate = (1 + expMinus) / (1 - expMinus);
            jacobianRow[paramOffset + 1] = n * jacobianRow[paramOffset] * (2 / theta - (1 / t) * intermediate);
        }
    }

}
