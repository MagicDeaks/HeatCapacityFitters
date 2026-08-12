package com.magicdeaks.heatcapacity.tabs;

import static com.magicdeaks.heatcapacity.util.PolyCurveFitter.evaluatePolynomial;

import com.magicdeaks.heatcapacity.models.CompositeSpecificHeatModel;
import com.magicdeaks.heatcapacity.models.HighTSpecificHeatModel;
import com.magicdeaks.heatcapacity.models.OverlapTableModel;
import com.magicdeaks.heatcapacity.models.ThermFuncTableModel;
import com.magicdeaks.heatcapacity.records.ThermFunctions;
import com.magicdeaks.heatcapacity.session.AnalysisSession;
import com.magicdeaks.heatcapacity.util.ScientificNotationRenderer;
import com.magicdeaks.heatcapacity.util.ThermCalc;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.*;

public class ThermCalcTab extends JPanel implements PropertyChangeListener {
    private AnalysisSession[] session;

    private JPanel leftPanel;
    private JPanel centrePanel;
    private JPanel centreTopPanel;
    private JPanel centreTopLeftPanel;
    private JPanel centreTopRightPanel;
    private JPanel centreBottomPanel;
    private JPanel centreBottomTopPanel;
    private JPanel centreBottomCentrePanel;

    private JTable lowOverlapTable;
    private JTable highOverlapTable;

    private JTable thermFuncTable;

    private JTextField lowField;
    private JTextField highField;

    private OverlapTableModel lowOverlapModel;
    private OverlapTableModel highOverlapModel;

    private ThermFuncTableModel thermFuncTableModel;

    private JButton overlapButton;
    private JButton thermButton;

    private double[][] lowMidOverlaps = new double[3][];
    private double[][] midHighOverlaps = new double[3][];

    private double lowOverlapT = 12;
    private double highOverlapT = 50;

    private double[] resultTemps = {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100, 110,
        120, 130, 140, 150, 160, 170, 180, 190, 200, 210, 220, 230, 240, 250, 260, 270, 273.15, 280,
        290, 298.15, 300
    };

    public ThermCalcTab(AnalysisSession[] session) {
        this.session = session;

        this.session[0].addPropertyChangeListener(this);

        initComponents();
    }

    public void updateSession() {
        if (session[0].getOverlaps() != null) {
            lowField.setText(String.valueOf(session[0].getOverlaps()[0]));
            highField.setText(String.valueOf(session[0].getOverlaps()[1]));
        }

        if (session[0].getThermFunctions() != null) {
            updateFunctions();
        }

        session[0].addPropertyChangeListener(this);
    }

    private void initComponents() {
        leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;

        centrePanel = new JPanel();
        centrePanel.setLayout(new BorderLayout());
        centreTopPanel = new JPanel();
        centrePanel.add(centreTopPanel, BorderLayout.NORTH);
        centreTopLeftPanel = new JPanel();
        centreTopPanel.add(centreTopLeftPanel, BorderLayout.WEST);
        centreTopRightPanel = new JPanel();
        centreTopPanel.add(centreTopRightPanel, BorderLayout.EAST);

        centreBottomPanel = new JPanel();
        centrePanel.add(centreBottomPanel, BorderLayout.SOUTH);
        centreBottomPanel.setLayout(new BorderLayout());
        centreBottomTopPanel = new JPanel();
        centreBottomPanel.add(centreBottomTopPanel, BorderLayout.NORTH);
        centreBottomCentrePanel = new JPanel();
        centreBottomPanel.add(centreBottomCentrePanel, BorderLayout.CENTER);

        overlapButton = new JButton("Find Overlaps");
        overlapButton.addActionListener(
                _ -> {
                    overlapButton.setEnabled(false);
                    try {
                        findOverlaps();
                        displayOverlaps();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    overlapButton.setEnabled(true);
                });

        thermButton = new JButton("Calculate Functions");
        thermButton.addActionListener(
                _ -> {
                    thermButton.setEnabled(false);
                    try {
                        lowOverlapT = Double.parseDouble(lowField.getText());
                        highOverlapT = Double.parseDouble(highField.getText());
                        session[0].setOverlaps(new double[] {lowOverlapT, highOverlapT});
                        calculateFunctions();
                        updateFunctions();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    thermButton.setEnabled(true);
                });

        lowField = new JTextField(5);
        highField = new JTextField(5);

        centreBottomTopPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        centreBottomTopPanel.add(new JLabel("Low/Mid Overlap:"));
        centreBottomTopPanel.add(lowField);
        centreBottomTopPanel.add(Box.createHorizontalStrut(15));
        centreBottomTopPanel.add(new JLabel("Mid/High Overlaps:"));
        centreBottomTopPanel.add(highField);
        centreBottomTopPanel.add(Box.createHorizontalStrut(15));
        centreBottomTopPanel.add(thermButton);

        gbc.gridx = 0;
        gbc.gridy = 0;
        leftPanel.add(overlapButton, gbc);

        this.setLayout(new BorderLayout());
        this.add(leftPanel, BorderLayout.WEST);
        this.add(centrePanel, BorderLayout.CENTER);

        lowOverlapModel = new OverlapTableModel();
        highOverlapModel = new OverlapTableModel();

        lowOverlapTable = new JTable(lowOverlapModel);
        highOverlapTable = new JTable(highOverlapModel);

        lowOverlapTable.setDefaultRenderer(Double.class, new ScientificNotationRenderer());
        highOverlapTable.setDefaultRenderer(Double.class, new ScientificNotationRenderer());

        thermFuncTableModel = new ThermFuncTableModel();
        thermFuncTable = new JTable(thermFuncTableModel);
        thermFuncTable.setDefaultRenderer(Double.class, new ScientificNotationRenderer());

        gbc.gridy = 1;
        leftPanel.add(new JLabel("Low/Mid Overlaps"), gbc);
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        leftPanel.add(new JScrollPane(lowOverlapTable), gbc);

        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridy = 3;
        leftPanel.add(new JLabel("Mid/High Overlaps"), gbc);
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        leftPanel.add(new JScrollPane(highOverlapTable), gbc);

        lowOverlapTable.setPreferredScrollableViewportSize(new Dimension(300, 100));
        highOverlapTable.setPreferredScrollableViewportSize(new Dimension(300, 100));

        centreBottomCentrePanel.setLayout(new BorderLayout());

        centreBottomCentrePanel.add(new JScrollPane(thermFuncTable), BorderLayout.CENTER);
        thermFuncTable.setPreferredScrollableViewportSize(new Dimension(600, 300));
        centreBottomCentrePanel.setBorder(
                BorderFactory.createTitledBorder("Thermodynamic Functions"));

        leftPanel.setBorder(BorderFactory.createTitledBorder("Find Overlaps"));
        centreBottomTopPanel.setBorder(BorderFactory.createTitledBorder("Set Overlaps"));
    }

    private void findOverlaps() {
        int points = 14751;

        double[][] lowMid = new double[4][];
        double[][] midHigh = new double[4][];

        int midSelect = session[0].getMidTSelect();

        double[] temps = new double[points];

        CompositeSpecificHeatModel lowModel = session[0].getLowTModel();

        HighTSpecificHeatModel highModel = session[0].getHighTModel();

        double min = 5;
        double max = 300.02;

        double dT = (max - min) / points;

        for (int i = 0; i < points; i++) {
            temps[i] = dT * (i) + min;
        }

        lowMid[0] = temps.clone();
        midHigh[0] = temps.clone();

        double[] lowHC = lowModel.value(temps, session[0].getLowTFit().coefficients());
        double[] highHC = highModel.value(temps, session[0].getHighTFit().coefficients());

        int[] midModel = session[0].getMidTModel()[midSelect];
        double[] midCoeffs = session[0].getMidTFit()[midSelect].coefficients();

        double[][] pairs = new double[midModel.length][];

        for (int i = 0; i < midModel.length; i++) {
            pairs[i] = new double[] {midCoeffs[i], midModel[i]};
        }

        double[] midHC = evaluatePolynomial(temps, pairs);

        lowMid[1] = new double[points];
        midHigh[1] = new double[points];

        lowMid[2] = new double[points - 1];
        midHigh[2] = new double[points - 1];

        lowMid[3] = new double[points - 2];
        midHigh[3] = new double[points - 2];

        double[] lowD = new double[points];
        double[] lowDD = new double[points];

        double[] midD = new double[points];
        double[] midDD = new double[points];

        double[] highD = new double[points];
        double[] highDD = new double[points];

        for (int i = 0; i < points; i++) {
            lowMid[1][i] = (lowHC[i] - midHC[i]) / midHC[i] * 100;
            midHigh[1][i] = (midHC[i] - highHC[i]) / highHC[i] * 100;

            if (i > 0) {
                lowD[i] = (lowHC[i] - lowHC[i - 1]) / dT;
                midD[i] = (midHC[i] - midHC[i - 1]) / dT;
                highD[i] = (highHC[i] - highHC[i - 1]) / dT;

                lowMid[2][i - 1] = (lowD[i] - midD[i]) / midD[i] * 100;

                midHigh[2][i - 1] = (midD[i] - highD[i]) / highD[i] * 100;
            }

            if (i > 1) {
                lowDD[i] = (lowD[i] - lowD[i - 1]) / dT;
                midDD[i] = (midD[i] - midD[i - 1]) / dT;
                highDD[i] = (highD[i] - highD[i - 1]) / dT;

                lowMid[3][i - 2] = (lowDD[i] - midDD[i]) / midDD[i] * 100;
                midHigh[3][i - 2] = (midDD[i] - highDD[i]) / highDD[i] * 100;
            }
        }

        lowMidOverlaps = lowMid;
        midHighOverlaps = midHigh;
    }

    private void displayOverlaps() {
        lowOverlapModel.clearAll();
        highOverlapModel.clearAll();

        for (int i = 1; i < lowMidOverlaps[0].length; i++) {
            if (lowMidOverlaps[1][i] * lowMidOverlaps[1][i - 1] < 0) {
                lowOverlapModel.addOverlap(
                        new double[] {
                            lowMidOverlaps[0][i - 1],
                            lowMidOverlaps[1][i - 1],
                            lowMidOverlaps[2][i - 1],
                            lowMidOverlaps[3][i - 1]
                        });
                lowOverlapModel.addOverlap(
                        new double[] {
                            lowMidOverlaps[0][i],
                            lowMidOverlaps[1][i],
                            lowMidOverlaps[2][i],
                            lowMidOverlaps[3][i]
                        });
            }
        }
        for (int i = 1; i < midHighOverlaps[0].length; i++) {
            if (midHighOverlaps[1][i] * midHighOverlaps[1][i - 1] < 0) {
                highOverlapModel.addOverlap(
                        new double[] {
                            midHighOverlaps[0][i - 1],
                            midHighOverlaps[1][i - 1],
                            midHighOverlaps[2][i - 1],
                            midHighOverlaps[3][i - 1]
                        });
                highOverlapModel.addOverlap(
                        new double[] {
                            midHighOverlaps[0][i],
                            midHighOverlaps[1][i],
                            midHighOverlaps[2][i],
                            midHighOverlaps[3][i]
                        });
            }
        }
    }

    private void calculateFunctions() {
        double[] lowTemps = ThermCalc.getTRange(resultTemps, 0, lowOverlapT);
        double[] midTemps = ThermCalc.getTRange(resultTemps, lowOverlapT, highOverlapT);
        double[] highTemps = ThermCalc.getTRange(resultTemps, highOverlapT, 310);

        double[] lowHC =
                session[0].getLowTModel().value(lowTemps, session[0].getLowTFit().coefficients());
        double[] highHC =
                session[0]
                        .getHighTModel()
                        .value(highTemps, session[0].getHighTFit().coefficients());

        int[] midModel = session[0].getMidTModel()[session[0].getMidTSelect()];
        double[] midCoeffs = session[0].getMidTFit()[session[0].getMidTSelect()].coefficients();

        double[][] pairs = new double[midModel.length][];

        for (int i = 0; i < midModel.length; i++) {
            pairs[i] = new double[] {midCoeffs[i], midModel[i]};
        }
        double[] midHC = evaluatePolynomial(midTemps, pairs);

        double[] finalHC = new double[resultTemps.length];

        System.out.println(Arrays.toString(lowTemps));
        System.out.println(Arrays.toString(lowHC));
        System.out.println(Arrays.toString(midTemps));
        System.out.println(Arrays.toString(midHC));
        System.out.println(Arrays.toString(highTemps));
        System.out.println(Arrays.toString(highHC));

        System.arraycopy(lowHC, 0, finalHC, 0, lowHC.length);
        System.arraycopy(midHC, 0, finalHC, lowHC.length, midHC.length);
        System.arraycopy(highHC, 0, finalHC, lowHC.length + midHC.length, highHC.length);

        double[] integralHC =
                ThermCalc.calculateHeatCapacity(
                        session[0], lowOverlapT, highOverlapT, 0, 310, 0.01);
        double[] integralTemps = ThermCalc.calculateTemperatures(0, 310, 0.01);

        double[] integralEnthalpies = ThermCalc.calculateEnthalpies(integralTemps, integralHC);
        double[] integralEntropies = ThermCalc.calculateEntropies(integralTemps, integralHC);
        double[] integralGibbs =
                ThermCalc.calculateGibbs(integralTemps, integralEnthalpies, integralEntropies);

        double[] finalEnthalpies = new double[resultTemps.length];
        double[] finalEntropies = new double[resultTemps.length];
        double[] finalGibbs = new double[resultTemps.length];

        for (int i = 0; i < finalEnthalpies.length; i++) {
            finalEnthalpies[i] = integralEnthalpies[findIdx(resultTemps[i], integralTemps)];
        }

        for (int i = 0; i < finalEntropies.length; i++) {
            finalEntropies[i] = integralEntropies[findIdx(resultTemps[i], integralTemps)];
        }

        for (int i = 0; i < finalGibbs.length; i++) {
            finalGibbs[i] = integralGibbs[findIdx(resultTemps[i], integralTemps)];
        }

        session[0].setThermFunctions(
                new ThermFunctions(
                        resultTemps, finalHC, finalEnthalpies, finalEntropies, finalGibbs));
    }

    private void updateFunctions() {
        ThermFunctions func = session[0].getThermFunctions();

        thermFuncTableModel.clearAll();

        for (int i = 0; i < func.temperatures().length; i++) {
            thermFuncTableModel.addFunctions(
                    new double[] {
                        func.temperatures()[i],
                        func.heatCapacities()[i],
                        func.enthalpies()[i],
                        func.entropies()[i],
                        func.gibbs()[i]
                    });
        }
    }

    private int findIdx(double target, double[] array) {
        double epsilon = 1e-9;

        for (int i = 0; i < array.length; i++) {
            double val = array[i];
            double diff = Math.abs(val - target);

            if (val == target
                    || diff <= epsilon
                    || diff <= Math.max(Math.abs(val), Math.abs(target)) * epsilon) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {}
}
