package com.magicdeaks.heatcapacity.session;

import com.magicdeaks.heatcapacity.models.CompositeSpecificHeatModel;
import com.magicdeaks.heatcapacity.models.HighTSpecificHeatModel;
import com.magicdeaks.heatcapacity.records.FitResult;
import com.magicdeaks.heatcapacity.records.HeatCapacityData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class AnalysisSession {
    private HeatCapacityData rawData;
    private FitResult lowTFit;
    private FitResult[] midTFit;
    private FitResult highTFit;
    private int midTSelect;

    private CompositeSpecificHeatModel lowTModel;
    private int[][] midTModel;
    private HighTSpecificHeatModel highTModel;

    private double atoms;
    private double molecularWeight;

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public int getMidTSelect() {
        return midTSelect;
    }

    public CompositeSpecificHeatModel getLowTModel() {
        return lowTModel;
    }
    
    public int[][] getMidTModel() {
        return midTModel;
    }

    public HighTSpecificHeatModel getHighTModel() {
        return highTModel;
    }

    public HeatCapacityData getRawData() {
        return rawData;
    }

    public double getAtoms() {
        return atoms;
    }

    public double getMolecularWeight() {
        return molecularWeight;
    }

    public void setMidTSelect(int select) {
        int oldSelect = this.midTSelect;
        this.midTSelect = select;

        support.firePropertyChange("midTSelect", oldSelect, select);
    }

    public void setLowTModel(CompositeSpecificHeatModel model) {
        CompositeSpecificHeatModel oldModel = this.lowTModel;
        this.lowTModel = model;

        support.firePropertyChange("lowTModel", oldModel, model);
    }

    public void setMidTModel(int[][] model) {
        int[][] oldModel = this.midTModel;
        this.midTModel = model;

        support.firePropertyChange("midTModel", oldModel, model);
    }

    public void setHighTModel(HighTSpecificHeatModel model) {
        HighTSpecificHeatModel oldModel = this.highTModel;
        this.highTModel = model;

        support.firePropertyChange("highTModel", oldModel, model);
    }

    public void setAtoms(double atoms) {
        double oldAtoms = this.atoms;
        this.atoms = atoms;

        support.firePropertyChange("atoms", oldAtoms, atoms);
    }

    public void setMolecularWeight(double molecularWeight) {
        double oldMolecularWeight = this.molecularWeight;
        this.molecularWeight = molecularWeight;

        support.firePropertyChange("molecularWeight", oldMolecularWeight, molecularWeight);
    }

    public void setRawData(HeatCapacityData rawData) {
        HeatCapacityData oldData = this.rawData;
        this.rawData = rawData;

        support.firePropertyChange("rawData", oldData, rawData);
    }

    public void setLowTFit(FitResult lowTFit) {
        FitResult oldFit = this.lowTFit;
        this.lowTFit = lowTFit;

        support.firePropertyChange("lowTFit", oldFit, lowTFit);
    }

    public void setMidTFit(FitResult midTFit, int degree) {
        FitResult[] oldFit = this.midTFit;
        this.midTFit[degree] = midTFit;

        support.firePropertyChange("midTFit", oldFit, midTFit);
    }

    public void setMidTFit(FitResult[] midTFit) {
        FitResult[] oldFit = this.midTFit;
        this.midTFit = midTFit;

        support.firePropertyChange("midTFit", oldFit, midTFit);
    }

    public void setHighTFit(FitResult highTFit) {
        FitResult oldFit = this.highTFit;
        this.highTFit = highTFit;

        support.firePropertyChange("highTFit", oldFit, highTFit);
    }

    public FitResult getLowTFit() {
        return lowTFit;
    }

    public FitResult getMidTFit(int degree) {
        return midTFit[degree];
    }

    public FitResult[] getMidTFit() {
        return midTFit;
    }

    public FitResult getHighTFit() {
        return highTFit;
    }
}
