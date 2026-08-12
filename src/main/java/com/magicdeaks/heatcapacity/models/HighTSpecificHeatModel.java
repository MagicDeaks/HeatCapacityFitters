package com.magicdeaks.heatcapacity.models;

import static com.magicdeaks.heatcapacity.models.HighTSpecificHeatModel.HighTFitModel.*;

import com.magicdeaks.heatcapacity.util.HighTFunctions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HighTSpecificHeatModel implements ParametricModel, Serializable {
    private static final long serialVersionUID = 1L;
    private static final double h = 0.2;

    public enum HighTFitModel {
        LINEAR,
        SQUARE,
        DEBYE,
        EINSTEIN
    }

    public static enum StandardModels {
        D_E_L_S(DEBYE, EINSTEIN, LINEAR, SQUARE),
        D_E_L(DEBYE, EINSTEIN, LINEAR),
        D_E_S(DEBYE, EINSTEIN, SQUARE),
        D_E(DEBYE, EINSTEIN),
        D_E_E(DEBYE, EINSTEIN, EINSTEIN),
        D_D_E_E(DEBYE, DEBYE, EINSTEIN, EINSTEIN);

        private final HighTFitModel[] models;

        StandardModels(HighTFitModel... models) {
            this.models = models;
        }

        public HighTFitModel[] getModels() {
            return models;
        }
    }

    private final List<ModelTerm> TERMS = new ArrayList<>();
    private final int TOTAL_PARAMETERS;

    public List<ModelTerm> getTerms() {
        return TERMS;
    }

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
            throw new IllegalArgumentException(
                    "Expected " + TOTAL_PARAMETERS + " parameters, got " + parameters.length);
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

    public abstract static class ModelTerm implements Serializable {
        private static final long serialVersionUID = 1L;
        protected final HighTFitModel model;
        protected final int paramOffset;

        public ModelTerm(HighTFitModel model, int paramOffset) {
            this.model = model;
            this.paramOffset = paramOffset;
        }

        public HighTFitModel getModel() {
            return model;
        }

        abstract int getParameterCount();

        abstract double evaluate(double t, double[] params);

        abstract void addJacobianDerivatives(double t, double[] params, double[] jacobianRow);

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ModelTerm model = (ModelTerm) o;
            return this.model == model.model && paramOffset == model.paramOffset;
        }

        @Override
        public int hashCode() {
            return Objects.hash(model, paramOffset);
        }
    }

    private ModelTerm createTerm(HighTFitModel model, int offset) {
        return switch (model) {
            case LINEAR -> new PolynomialTerm(model, offset, 1);
            case SQUARE -> new PolynomialTerm(model, offset, 2);
            case DEBYE -> new DebyeTerm(model, offset);
            case EINSTEIN -> new EinsteinTerm(model, offset);
            default -> throw new IllegalArgumentException("Unknown Model: " + model);
        };
    }

    public static class PolynomialTerm extends ModelTerm {
        private final double POWER;

        public PolynomialTerm(HighTFitModel model, int offset, double power) {
            super(model, offset);
            this.POWER = power;
        }

        @Override
        int getParameterCount() {
            return 1;
        }

        @Override
        double evaluate(double t, double[] params) {
            return params[paramOffset] * Math.pow(t, POWER);
        }

        @Override
        void addJacobianDerivatives(double t, double[] params, double[] jacobianRow) {
            jacobianRow[paramOffset] = Math.pow(t, POWER);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PolynomialTerm poly = (PolynomialTerm) o;
            return this.model == poly.model
                    && POWER == poly.POWER
                    && paramOffset == poly.paramOffset;
        }

        @Override
        public int hashCode() {
            return Objects.hash(model, POWER, paramOffset);
        }
    }

    public static class DebyeTerm extends ModelTerm {
        public DebyeTerm(HighTFitModel model, int offset) {
            super(model, offset);
        }

        @Override
        int getParameterCount() {
            return 2;
        }

        @Override
        double evaluate(double t, double[] params) {
            double n = params[paramOffset];
            double theta = params[paramOffset + 1];

            return n * HighTFunctions.calculateDebye(theta, t, h);
        }

        @Override
        void addJacobianDerivatives(double t, double[] params, double[] jacobianRow) {
            double n = params[paramOffset];
            double theta = params[paramOffset + 1];

            double expMinus = Math.exp(-theta / t);

            jacobianRow[paramOffset] = HighTFunctions.calculateDebye(theta, t, h);

            jacobianRow[paramOffset + 1] = -3 / theta * jacobianRow[paramOffset];

            double intermediate = expMinus / ((1 - expMinus) * (1 - expMinus));
            jacobianRow[paramOffset + 1] += n * 9 * 8.314472 * theta / (t * t) * intermediate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            DebyeTerm deb = (DebyeTerm) o;
            return this.model == deb.model && this.paramOffset == deb.paramOffset;
        }

        @Override
        public int hashCode() {
            return Objects.hash(model, paramOffset);
        }
    }

    public static class EinsteinTerm extends ModelTerm {
        public EinsteinTerm(HighTFitModel model, int offset) {
            super(model, offset);
        }

        @Override
        int getParameterCount() {
            return 2;
        }

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
            jacobianRow[paramOffset + 1] =
                    n * jacobianRow[paramOffset] * (2 / theta - (1 / t) * intermediate);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            EinsteinTerm ein = (EinsteinTerm) o;
            return this.model == ein.model && this.paramOffset == ein.paramOffset;
        }

        @Override
        public int hashCode() {
            return Objects.hash(model, paramOffset);
        }
    }
}
