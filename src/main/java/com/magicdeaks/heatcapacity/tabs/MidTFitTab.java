package com.magicdeaks.heatcapacity.tabs;

import com.magicdeaks.heatcapacity.models.PolynomialTableModel;
import com.magicdeaks.heatcapacity.records.FitResult;
import com.magicdeaks.heatcapacity.records.HeatCapacityData;
import com.magicdeaks.heatcapacity.session.AnalysisSession;
import com.magicdeaks.heatcapacity.util.ScientificNotationRenderer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.data.xy.DefaultXYDataset;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static com.magicdeaks.heatcapacity.util.Deviations.getDeviations;
import static com.magicdeaks.heatcapacity.util.PolyCurveFitter.evaluatePolynomial;
import static com.magicdeaks.heatcapacity.util.PolyCurveFitter.fitOrthogonalPolynomial;

public class MidTFitTab extends JPanel implements PropertyChangeListener {
    private final AnalysisSession session;

    private JTextField minTempField, maxTempField;
    private JButton fitButton;
    private JLabel statusLabel;
    private ChartPanel graphPanel;
    private ChartPanel devPanel;
    private JFreeChart midTGraph;
    private JFreeChart devGraph;
    private DefaultXYDataset dataset = new DefaultXYDataset();
    private DefaultXYDataset deviations = new DefaultXYDataset();
    private final double[] EMPTY_ROW = new double[13];

    private JTextField startPowerField, incrField;

    private final int NUM_FITS = 11;
    private int[][] powers = new int[NUM_FITS][];

    private PolynomialTableModel polyTableModel;
    private JTable polyTable;

    private JComboBox<String> fitSelector;

    public MidTFitTab(AnalysisSession session) {
        this.session = session;

        this.session.addPropertyChangeListener(this);

        initComponents();
        buildLayout();

        updateChart();
    }

    private void initComponents() {
        minTempField = new JTextField("5", 5);
        maxTempField = new JTextField("65", 5);

        startPowerField = new JTextField("0", 5);
        incrField = new JTextField("1", 5);

        fitButton = new JButton("Run Mid T Fits");
        statusLabel = new JLabel("Waiting for data...");

        String[] fitNames = new String[NUM_FITS];
        for (int i = 0; i < NUM_FITS; i++) {
            fitNames[i] = "Fit-" + i;
        }
        fitSelector = new JComboBox<>(fitNames);
        fitSelector.setEnabled(false);
        fitSelector.addActionListener(_ -> updateChart());

        fitButton.setEnabled(false);
        fitButton.addActionListener(_ -> executeFit());

        midTGraph = ChartFactory.createXYLineChart("Mid T Graph", "T (K)", "Cp (J/mol·K)", dataset);
        devGraph = ChartFactory.createXYLineChart("Deviations", "T (K)", "(Cp(fit) - Cp) / Cp(fit)", deviations);

        graphPanel = new ChartPanel(midTGraph);
        graphPanel.setBackground(Color.WHITE);
        graphPanel.setBorder(BorderFactory.createTitledBorder("Mid T Graph"));

        devPanel = new ChartPanel(devGraph);
        devPanel.setBackground(Color.WHITE);
        devPanel.setBorder(BorderFactory.createTitledBorder("Deviations"));

        XYPlot plot = midTGraph.getXYPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();

        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesLinesVisible(1, true);

        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesVisible(1, false);

        plot.setRenderer(renderer);

        XYPlot devPlot = devGraph.getXYPlot();
        XYLineAndShapeRenderer devRenderer = (XYLineAndShapeRenderer) devPlot.getRenderer();

        devRenderer.setSeriesLinesVisible(0, false);
        devRenderer.setSeriesShapesVisible(0, true);

        devPlot.setRenderer(devRenderer);
        
        ValueMarker zeroLine = new ValueMarker(0.0);
        zeroLine.setPaint(Color.BLACK);
        zeroLine.setStroke(new BasicStroke(1.5f));
        devPlot.addRangeMarker(zeroLine, Layer.BACKGROUND);

        polyTableModel = new PolynomialTableModel();
        polyTable = new JTable(polyTableModel);
        polyTable.setDefaultRenderer(Double.class, new ScientificNotationRenderer());

        polyTable.setPreferredScrollableViewportSize(new Dimension(500, 175));

        for (int i = 0; i < NUM_FITS; i++) {
            polyTableModel.addFit("Fit-"+ i, EMPTY_ROW);
        }
    }

    private void buildLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel chartWrapper = new JPanel(new GridLayout(1, 2));
        chartWrapper.add(graphPanel);
        chartWrapper.add(devPanel);
        add(chartWrapper, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout(5, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Fitting Controls"));

        JPanel topConfigPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topConfigPanel.add(new JLabel("Min T:")); topConfigPanel.add(minTempField);
        topConfigPanel.add(new JLabel("Max T:")); topConfigPanel.add(maxTempField);
        topConfigPanel.add(Box.createHorizontalStrut(15));
        topConfigPanel.add(new JLabel("Start Power:")); topConfigPanel.add(startPowerField);
        topConfigPanel.add(new JLabel("Increment:")); topConfigPanel.add(incrField);

        controlPanel.add(topConfigPanel, BorderLayout.NORTH);

        JPanel centrePanel = new JPanel(new BorderLayout(5, 5));

        JScrollPane tableScroll = new JScrollPane(polyTable);
        centrePanel.add(tableScroll, BorderLayout.CENTER);

        controlPanel.add(centrePanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.add(new JLabel("Fit:"));
        actionPanel.add(fitSelector);
        actionPanel.add(Box.createHorizontalStrut(15));
        actionPanel.add(statusLabel);
        actionPanel.add(fitButton);
        controlPanel.add(actionPanel, BorderLayout.SOUTH);

        add(controlPanel, BorderLayout.SOUTH);
    }

    private void executeFit() {
        fitSelector.setEnabled(false);
        fitButton.setEnabled(false);
        statusLabel.setText("Fitting data...");

        SwingWorker<FitResult[], Void> worker = new SwingWorker<>() {
            @Override
            protected FitResult[] doInBackground() {
                double minT = Double.parseDouble(minTempField.getText());
                double maxT = Double.parseDouble(maxTempField.getText());

                FitResult[] results = new FitResult[NUM_FITS];
                int startPower = Integer.parseInt(startPowerField.getText());
                int incr = Integer.parseInt(incrField.getText());

                HeatCapacityData data = getMidTData(session, minT, maxT);

                for (int i = 0; i < NUM_FITS; i++) {
                    results[i] = fitOrthogonalPolynomial(data, startPower, incr, i + 1);

                    powers[i] = new int[i+1];
                    for (int j = 0; j <= i; j++) {
                        powers[i][j] = startPower + incr * j;
                    }
                }

                System.out.println(Arrays.deepToString(powers));
                return results;
            }

            @Override
            protected void done() {
                try {
                    FitResult[] results = get();

                    session.setMidTFit(results);
                    statusLabel.setText("Data fitted successfully!");

                    double[][] fittedCoeffs = new double[results.length][];
                    for (int i = 0; i < results.length; i++) {
                        fittedCoeffs[i] = results[i].coefficients();
                    }

                    for (int i = 0; i < fittedCoeffs.length; i++) {
                        for (int j = 0; j < fittedCoeffs[i].length; j++) {
                            polyTableModel.setValueAt(fittedCoeffs[i][j], i, j + 1);
                        }

                        polyTableModel.setValueAt(results[i].pctRMS(), i, 12);
                    }

                    fitSelector.setEnabled(true);
                    updateChart();

                } catch (InterruptedException | ExecutionException e) {
                    statusLabel.setText("Error during fitting.");
                    JOptionPane.showMessageDialog(MidTFitTab.this,
                            "Fitting failed: " + e.getMessage(),
                            "Calculate Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    fitButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void updateChart() {
        double minT;
        double maxT;
        try {
            minT = Double.parseDouble(minTempField.getText());
            maxT = Double.parseDouble(maxTempField.getText());
        } catch (NumberFormatException _) {
            return;
        }

        DefaultXYDataset currentDataset = new DefaultXYDataset();
        DefaultXYDataset currentDeviations = new DefaultXYDataset();

        HeatCapacityData rawData = (session.getRawData() != null) ? getMidTData(session, minT, maxT) : null;

        if (rawData != null) {
            if (rawData.temperatures() != null) {
                double[][] rawDataArrays = new double[][]{rawData.temperatures(), rawData.heatCapacities()};
                dataset.addSeries("Heat Capacity", rawDataArrays);
                fitButton.setEnabled(true);
                statusLabel.setText("Data loaded. Ready to fit.");
            }
        }

        FitResult[] fitResults = session.getMidTFit();
        if (fitResults != null) {
            if (Arrays.stream(fitResults).noneMatch(Objects::isNull)) {
                int selectedIdx = fitSelector.getSelectedIndex();

                if (selectedIdx >= 0 && selectedIdx < fitResults.length) {
                    try {
                        int pointsCount = 200;

                        double[] fitX = new double[pointsCount];
                        double step = (maxT - minT) / (pointsCount - 1);

                        for (int i = 0; i < pointsCount; i++) {
                            fitX[i] = minT + i * step;
                        }

                        double[] doublePowers = new double[powers[selectedIdx].length];

                        for (int j = 0; j < powers[selectedIdx].length; j++) {
                            doublePowers[j] = powers[selectedIdx][j];
                        }

                        double[][] pairs = new double[fitResults[selectedIdx].coefficients().length][];
                        for (int j = 0; j < fitResults[selectedIdx].coefficients().length; j++) {
                            pairs[j] = new double[]{fitResults[selectedIdx].coefficients()[j], doublePowers[j]};
                            System.out.println(Arrays.toString(pairs[j]));
                        }

                        double[] fitY = evaluatePolynomial(fitX, pairs);

                        currentDataset.addSeries("Heat Capacity", new double[][]{rawData.temperatures(), rawData.heatCapacities()});
                        currentDataset.addSeries("Fit-" + selectedIdx, new double[][]{fitX, fitY});

                        if (rawData != null) {
                            if (rawData.temperatures() != null) {
                                double[][] deviationsSet = getDeviations(powers[selectedIdx], fitResults[selectedIdx].coefficients(), rawData);

                                currentDeviations.addSeries("Dev-"+selectedIdx, deviationsSet);
                            }
                        }
                    } catch (NumberFormatException _) {

                    }
                }
            }
        }
        this.dataset = currentDataset;
        this.deviations = currentDeviations;

        midTGraph.getXYPlot().setDataset(this.dataset);
        devGraph.getXYPlot().setDataset(this.deviations);

        this.revalidate();
        this.repaint();
    }

    private HeatCapacityData getMidTData(AnalysisSession session, double minT, double maxT) {
        HeatCapacityData data = session.getRawData();

        int minIdx = IntStream.range(0, data.temperatures().length)
                .filter(i -> data.temperatures()[i] >= minT)
                .findFirst()
                .orElse(0);
        int maxIdx = IntStream.range(0, data.temperatures().length)
                .filter(i -> data.temperatures()[i] <= maxT)
                .reduce((_, second) -> second)
                .orElse(data.temperatures().length - 1);

        double[] midT = Arrays.copyOfRange(data.temperatures(), minIdx, maxIdx+1);
        double[] midHC = Arrays.copyOfRange(data.heatCapacities(), minIdx, maxIdx+1);

        return new HeatCapacityData(midT, midHC);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("rawData".equals(event.getPropertyName())) {
            updateChart();
        }
    }
}
