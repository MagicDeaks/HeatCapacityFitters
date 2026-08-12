package com.magicdeaks.heatcapacity.session;

import com.magicdeaks.heatcapacity.models.CompositeSpecificHeatModel;
import com.magicdeaks.heatcapacity.models.HighTSpecificHeatModel;
import com.magicdeaks.heatcapacity.records.FitResult;
import com.magicdeaks.heatcapacity.records.HeatCapacityData;
import com.magicdeaks.heatcapacity.records.ThermFunctions;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class AnalysisSession implements Serializable {
    private static final long serialVersionUID = 1L;
    private HeatCapacityData rawData;
    private FitResult lowTFit;
    private FitResult[] midTFit;
    private FitResult highTFit;
    private int midTSelect;
    private double[] overlaps;

    private CompositeSpecificHeatModel lowTModel;
    private int[][] midTModel;
    private HighTSpecificHeatModel highTModel;

    private double atoms;
    private double molecularWeight;
    private String molecularFormula;
    private String path;

    private double sampleMass;
    private double copperMass;
    private boolean copperSub;

    private double[] lowTRange;
    private double[] midTRange;
    private double[] highTRange;

    private int[] orthoParams;

    private ThermFunctions thermFunctions;

    private transient PropertyChangeSupport support = new PropertyChangeSupport(this);

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.support = new PropertyChangeSupport(this);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public double[] getLowTRange() {
        return lowTRange;
    }

    public double[] getMidTRange() {
        return midTRange;
    }

    public double[] getHighTRange() {
        return highTRange;
    }

    public int[] getOrthoParams() {
        return orthoParams;
    }

    public double getSampleMass() {
        return sampleMass;
    }

    public double getCopperMass() {
        return copperMass;
    }

    public boolean getCopperSub() {
        return copperSub;
    }

    public String getPath() {
        return path;
    }

    public String getMolecularFormula() {
        return molecularFormula;
    }

    public double[] getOverlaps() {
        return overlaps;
    }

    public ThermFunctions getThermFunctions() {
        return thermFunctions;
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

    public void setLowTRange(double[] range) {
        if (range.length != 2) throw new IllegalArgumentException("Array is not size 2.");
        double[] oldRange = this.lowTRange;
        this.lowTRange = range;

        support.firePropertyChange("lowTRange", oldRange, range);
    }

    public void setMidTRange(double[] range) {
        if (range.length != 2) throw new IllegalArgumentException("Array is not size 2.");
        double[] oldRange = this.midTRange;
        this.midTRange = range;

        support.firePropertyChange("midTRange", oldRange, range);
    }

    public void setHighTRange(double[] range) {
        if (range.length != 2) throw new IllegalArgumentException("Array is not size 2.");
        double[] oldRange = this.highTRange;
        this.highTRange = range;

        support.firePropertyChange("highTRange", oldRange, range);
    }

    public void setOrthoParams(int[] params) {
        if (params.length != 2) throw new IllegalArgumentException("Array is not size 2.");
        int[] oldParams = this.orthoParams;
        this.orthoParams = params;

        support.firePropertyChange("orthoParams", oldParams, params);
    }

    public void setSampleMass(double mass) {
        double oldMass = this.sampleMass;
        this.sampleMass = mass;

        support.firePropertyChange("sampleMass", oldMass, mass);
    }

    public void setCopperMass(double mass) {
        double oldMass = this.copperMass;
        this.copperMass = mass;

        support.firePropertyChange("copperMass", oldMass, mass);
    }

    public void setCopperSub(boolean sub) {
        boolean oldSub = this.copperSub;
        this.copperSub = sub;

        support.firePropertyChange("copperSub", oldSub, sub);
    }

    public void setPath(String path) {
        String oldPath = this.path;
        this.path = path;

        support.firePropertyChange("path", oldPath, path);
    }

    public void setMolecularFormula(String formula) {
        String oldFormula = this.molecularFormula;
        this.molecularFormula = formula;

        support.firePropertyChange("molecularFormula", oldFormula, formula);
    }

    public void setOverlaps(double[] overlaps) {
        if (overlaps.length != 2) throw new IllegalArgumentException("Array is not size 2.");
        double[] oldOverlaps = this.overlaps;
        this.overlaps = overlaps;

        support.firePropertyChange("overlaps", oldOverlaps, overlaps);
    }

    public void setThermFunctions(ThermFunctions func) {
        ThermFunctions oldFunc = this.thermFunctions;
        this.thermFunctions = func;

        support.firePropertyChange("thermFunctions", oldFunc, func);
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
