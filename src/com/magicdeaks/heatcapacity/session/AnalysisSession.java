package com.magicdeaks.heatcapacity.session;

import com.magicdeaks.heatcapacity.records.FitResult;
import com.magicdeaks.heatcapacity.records.HeatCapacityData;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class AnalysisSession {
    private HeatCapacityData rawData;
    private FitResult lowTFit;
    private FitResult midTFit;
    private FitResult highTFit;

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public HeatCapacityData getRawData() {
        return rawData;
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

    public void setMidTFit(FitResult midTFit) {
        FitResult oldFit = this.midTFit;
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

    public FitResult getMidTFit() {
        return midTFit;
    }

    public FitResult getHighTFit() {
        return highTFit;
    }
}
