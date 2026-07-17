package com.magicdeaks.heatcapacity.tabs;

import com.magicdeaks.heatcapacity.models.CompositeSpecificHeatModel;
import com.magicdeaks.heatcapacity.models.HighTSpecificHeatModel;
import com.magicdeaks.heatcapacity.session.AnalysisSession;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static com.magicdeaks.heatcapacity.util.PolyCurveFitter.evaluatePolynomial;

public class ThermCalcTab extends JPanel implements PropertyChangeListener {
    private final AnalysisSession session;

    private JPanel leftPanel;
    private JPanel centrePanel;
    private JPanel centreCentrePanel;
    private JPanel centreCentreLeftPanel;
    private JPanel centreCentreRightPanel;
    private JPanel centreBottomPanel;
    private JPanel centreBottomTopPanel;
    private JPanel centreBottomCentrePanel;

    private JButton overlapButton;

    private double[][] lowMidOverlaps = new double[3][];
    private double[][] midHighOverlaps = new double[3][];

    public ThermCalcTab(AnalysisSession session) {
        this.session = session;

        this.session.addPropertyChangeListener(this);

        initComponents();
    }

    private void initComponents() {
        leftPanel = new JPanel();

        centrePanel = new JPanel();
        centrePanel.setLayout(new BorderLayout());
        centreCentrePanel = new JPanel(); centrePanel.add(centreCentrePanel, BorderLayout.CENTER);
        centreCentreLeftPanel = new JPanel(); centreCentrePanel.add(centreCentreLeftPanel, BorderLayout.WEST);
        centreCentreRightPanel = new JPanel (); centreCentrePanel.add(centreCentreRightPanel, BorderLayout.EAST);

        centreBottomPanel = new JPanel(); centrePanel.add(centreBottomPanel, BorderLayout.SOUTH);
        centreBottomPanel.setLayout(new BorderLayout());
        centreBottomTopPanel = new JPanel(); centreBottomPanel.add(centreBottomTopPanel, BorderLayout.NORTH);
        centreBottomCentrePanel = new JPanel(); centreBottomPanel.add(centreBottomCentrePanel, BorderLayout.CENTER);

        overlapButton = new JButton("Find Overlaps");
        overlapButton.addActionListener(_ -> {
            findOverlaps();
        });

        leftPanel.add(overlapButton);

        this.setLayout(new BorderLayout());
        this.add(leftPanel, BorderLayout.WEST);
        this.add(centrePanel, BorderLayout.CENTER);
    }

    private void findOverlaps() {
        int points = 3000;

        double[][] lowMid = new double[3][];
        double[][] midHigh = new double[3][];

        int midSelect = 8;

        double[] temps = new double[points];

        CompositeSpecificHeatModel lowModel = session.getLowTModel();
        int[] midModel = session.getMidTModel()[midSelect];
        double[] midCoeffs = session.getMidTFit()[midSelect].coefficients();
        HighTSpecificHeatModel highModel = session.getHighTModel();

        double min = 0;
        double max = 300;

        double dT = (max - min) / points;

        for (int i = 0; i < points; i++) {
            temps[i] = dT * (i+1);
        }

        double[] lowHC = lowModel.value(temps, session.getLowTFit().coefficients());
        double[] highHC = highModel.value(temps, session.getHighTFit().coefficients());
        
        double[][] pairs = new double[midModel.length][];

        for (int i = 0; i < midModel.length; i++) {
            pairs[i] = new double[]{midModel[i], midCoeffs[i]};
        }

        double[] midHC = evaluatePolynomial(temps, pairs);
        
        lowMid[0] = new double[points];
        midHigh[0] = new double[points];

        lowMid[1] = new double[points-1];
        midHigh[1] = new double[points-1];

        lowMid[2] = new double[points-2];
        midHigh[2] = new double[points-2];

        double[] lowD = new double[points-1];
        double[] lowDD = new double[points-2];

        double[] midD = new double[points-1];
        double[] midDD = new double[points-2];

        double[] highD = new double[points-1];
        double[] highDD = new double[points-2];


        for (int i = 0; i < points; i++) {
            lowMid[0][i] = (lowHC[i] - midHC[i]) / midHC[i] * 100;
            midHigh[0][i] = (midHC[i] - highHC[i]) / highHC[i] * 100;

            if (i > 0) {
                lowD[i] = (lowHC[i] - lowHC[i-1]) / dT;
                midD[i] = (midHC[i] - midHC[i-1]) / dT;
                highD[i] = (highHC[i] - highHC[i-1]) / dT;

                lowMid[1][i-1] = (lowD[i] - midD[i]) / midD[i] * 100;

                midHigh[1][i-1] = (midD[i] - highD[i]) / highD[i] * 100;
            }

            if (i > 1) {
                lowDD[i] = (lowD[i] - lowD[i-1]) / dT;
                midDD[i] = (midD[i] - midD[i-1]) / dT;
                highDD[i] = (highD[i] - highD[i-1]) / dT;

                lowMid[2][i-2] = (lowDD[i] - midDD[i]) / midDD[i] * 100;
                midHigh[2][i-2] = (midDD[i] - highDD[i]) / highDD[i] * 100;
            }
        }

        lowMidOverlaps = lowMid;
        midHighOverlaps = midHigh;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
    }
}
