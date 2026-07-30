package com.magicdeaks.heatcapacity.tabs;

import static com.magicdeaks.heatcapacity.util.Deviations.getDeviations;

import com.magicdeaks.heatcapacity.models.HighTSpecificHeatModel;
import com.magicdeaks.heatcapacity.models.ParameterTableModel;
import com.magicdeaks.heatcapacity.records.FitResult;
import com.magicdeaks.heatcapacity.records.HeatCapacityData;
import com.magicdeaks.heatcapacity.session.AnalysisSession;
import com.magicdeaks.heatcapacity.util.LmCurveFitter;
import com.magicdeaks.heatcapacity.util.ScientificNotationRenderer;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;
import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.data.xy.DefaultXYDataset;

public class HighTFitTab extends JPanel implements PropertyChangeListener {
  private final AnalysisSession session;

  private JTextField minTempField, maxTempField;
  private JSpinner iterSpinner, calcSpinner;
  private JButton fitButton, addTermButton, clearModelButton;
  private JComboBox<HighTSpecificHeatModel.HighTFitModel> termSelector;
  private JLabel statusLabel;
  private ChartPanel graphPanel;
  private ChartPanel devPanel;
  private JFreeChart highTGraph;
  private JFreeChart devGraph;
  private final DefaultXYDataset dataset = new DefaultXYDataset();
  private final DefaultXYDataset deviations = new DefaultXYDataset();
  private double pctRMS = 0.0;
  private int iterations = 0;
  private JLabel rmsLabel;
  private JLabel iterLabel;

  private JTable paramTable;
  private ParameterTableModel paramTableModel;
  private final java.util.List<HighTSpecificHeatModel.HighTFitModel> activeModelTerms =
      new ArrayList<>();

  public HighTFitTab(AnalysisSession session) {
    this.session = session;

    this.session.addPropertyChangeListener(this);

    initComponents();
    buildLayout();

    updateChart();
  }

  private void initComponents() {
    minTempField = new JTextField("40", 5);
    maxTempField = new JTextField("305", 5);
    iterSpinner = new JSpinner(new SpinnerNumberModel(100000, 100, 10000000, 100));
    calcSpinner = new JSpinner(new SpinnerNumberModel(1000000, 1000, 100000000, 1000));

    termSelector = new JComboBox<>(HighTSpecificHeatModel.HighTFitModel.values());
    addTermButton = new JButton("Add Term");
    clearModelButton = new JButton("Clear Model");

    addTermButton.addActionListener(_ -> addSelectedTerm());
    clearModelButton.addActionListener(_ -> clearModel());

    fitButton = new JButton("Run High T Fits");
    statusLabel = new JLabel("Waiting for data...");

    rmsLabel = new JLabel("RMS: " + pctRMS);
    iterLabel = new JLabel("Iter: " + iterations);

    fitButton.setEnabled(false);
    fitButton.addActionListener(_ -> executeFit());

    highTGraph = ChartFactory.createXYLineChart("High T Graph", "T (K)", "Cp (J/mol·K)", dataset);
    devGraph =
        ChartFactory.createXYLineChart(
            "Deviations", "T (K)", "(Cp(fit) - Cp) / Cp(fit)", deviations);

    graphPanel = new ChartPanel(highTGraph);
    graphPanel.setBackground(Color.WHITE);
    graphPanel.setBorder(BorderFactory.createTitledBorder("High T Graph"));

    devPanel = new ChartPanel(devGraph);
    devPanel.setBackground(Color.WHITE);
    devPanel.setBorder(BorderFactory.createTitledBorder("Deviations"));

    XYPlot plot = highTGraph.getXYPlot();
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

    paramTableModel = new ParameterTableModel();
    paramTable = new JTable(paramTableModel);
    paramTable.setDefaultRenderer(Double.class, new ScientificNotationRenderer());

    paramTable.setPreferredScrollableViewportSize(new Dimension(500, 150));
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
    JPanel northWrapper = new JPanel(new GridLayout(2, 1));

    JPanel topConfigPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    topConfigPanel.add(new JLabel("Min T:"));
    topConfigPanel.add(minTempField);
    topConfigPanel.add(new JLabel("Max T:"));
    topConfigPanel.add(maxTempField);
    topConfigPanel.add(Box.createHorizontalStrut(15));
    topConfigPanel.add(new JLabel("Max Iters:"));
    topConfigPanel.add(iterSpinner);
    topConfigPanel.add(new JLabel("Max Calcs:"));
    topConfigPanel.add(calcSpinner);
    topConfigPanel.add(Box.createHorizontalStrut(15));
    topConfigPanel.add(rmsLabel);
    topConfigPanel.add(iterLabel);

    JPanel builderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    builderPanel.add(new JLabel("Model Term:"));
    builderPanel.add(termSelector);
    builderPanel.add(addTermButton);
    builderPanel.add(clearModelButton);

    northWrapper.add(topConfigPanel);
    northWrapper.add(builderPanel);
    controlPanel.add(northWrapper, BorderLayout.NORTH);

    JPanel centrePanel = new JPanel(new BorderLayout(5, 5));

    JScrollPane tableScroll = new JScrollPane(paramTable);
    centrePanel.add(tableScroll, BorderLayout.CENTER);

    controlPanel.add(centrePanel, BorderLayout.CENTER);

    JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    actionPanel.add(statusLabel);
    actionPanel.add(fitButton);
    controlPanel.add(actionPanel, BorderLayout.SOUTH);

    add(controlPanel, BorderLayout.SOUTH);
  }

  private void addSelectedTerm() {
    if (paramTable.isEditing()) {
      paramTable.getCellEditor().stopCellEditing();
    }

    HighTSpecificHeatModel.HighTFitModel selectedModel =
        (HighTSpecificHeatModel.HighTFitModel) termSelector.getSelectedItem();
    if (selectedModel == null) return;

    switch (selectedModel) {
      case LINEAR:
        paramTableModel.addParameter("Linear", 0.001, 0.0, 100.0, false);
        break;
      case SQUARE:
        paramTableModel.addParameter("Square", 0.001, 0.0, 100.0, false);
        break;
      case DEBYE:
        paramTableModel.addParameter("m", session.getAtoms() / 3, 0.0, session.getAtoms(), false);
        paramTableModel.addParameter("θ_D", 150, 0.0, 1000.0, false);

        break;
      case EINSTEIN:
        if (activeModelTerms.contains(selectedModel)) {
          paramTableModel.addParameter("n", session.getAtoms() / 3, 0.0, session.getAtoms(), false);
          paramTableModel.addParameter("θ_E", 1000, 0.0, 4000.0, false);
          break;
        }
        paramTableModel.addParameter("n", session.getAtoms() / 3, 0.0, session.getAtoms(), false);
        paramTableModel.addParameter("θ_E", 300, 0.0, 4000.0, false);

        break;
      default:
        paramTableModel.addParameter(selectedModel.name() + " Coeff", 1.0, -10.0, 10.0, false);
        break;
    }

    activeModelTerms.add(selectedModel);
  }

  private void clearModel() {
    if (paramTable.isEditing()) {
      paramTable.getCellEditor().stopCellEditing();
    }

    activeModelTerms.clear();
    paramTableModel.clearAll();
  }

  private void executeFit() {
    if (paramTable.isEditing()) {
      paramTable.getCellEditor().stopCellEditing();
    }

    if (activeModelTerms.isEmpty()) {
      JOptionPane.showMessageDialog(
          this, "Please select at least one model term.", "No Model", JOptionPane.ERROR_MESSAGE);
      return;
    }

    double minT = Double.parseDouble(minTempField.getText());
    double maxT = Double.parseDouble(maxTempField.getText());
    int maxIters = (int) iterSpinner.getValue();
    int maxCalcs = (int) calcSpinner.getValue();

    fitButton.setEnabled(false);
    statusLabel.setText("Fitting data...");

    SwingWorker<FitResult, Void> worker =
        new SwingWorker<>() {
          @Override
          protected FitResult doInBackground() {
            HighTSpecificHeatModel.HighTFitModel[] modelArray =
                activeModelTerms.toArray(new HighTSpecificHeatModel.HighTFitModel[0]);
            HighTSpecificHeatModel highTModel = new HighTSpecificHeatModel(modelArray);
            session.setHighTModel(highTModel);

            double[] initialParams = paramTableModel.getColumnDataAsDouble(1);
            double[] lowerBounds = paramTableModel.getColumnDataAsDouble(2);
            double[] upperBounds = paramTableModel.getColumnDataAsDouble(3);
            boolean[] fixedParams = paramTableModel.getColumnDataAsBoolean(4);

            if (initialParams.length != highTModel.getTotalParameters()) {
              throw new IllegalStateException("Table rows do not match model parameter count.");
            }

            HeatCapacityData highTData = getHighTData(session, minT, maxT);

            return LmCurveFitter.fit(
                highTModel,
                highTData,
                initialParams,
                fixedParams,
                lowerBounds,
                upperBounds,
                maxIters,
                maxCalcs);
          }

          @Override
          protected void done() {
            try {
              FitResult results = get();

              session.setHighTFit(results);
              statusLabel.setText("Data fitted successfully!");

              double[] fittedCoeffs = results.coefficients();
              for (int i = 0; i < fittedCoeffs.length; i++) {
                if (i < paramTableModel.getRowCount()) {
                  paramTableModel.setValueAt(fittedCoeffs[i], i, 1);
                }
              }

              updateChart();

            } catch (InterruptedException | ExecutionException e) {
              statusLabel.setText("Error during fitting.");
              JOptionPane.showMessageDialog(
                  HighTFitTab.this,
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
    double minT = Double.parseDouble(minTempField.getText());
    double maxT = Double.parseDouble(maxTempField.getText());

    HeatCapacityData rawData =
        (session.getRawData() != null) ? getHighTData(session, minT, maxT) : null;
    if (rawData != null) {
      if (rawData.temperatures() != null) {
        double[][] rawDataArrays =
            new double[][] {rawData.temperatures(), rawData.heatCapacities()};
        dataset.addSeries("Heat Capacity", rawDataArrays);
        fitButton.setEnabled(true);
        statusLabel.setText("Data loaded. Ready to fit.");
      }
    }

    FitResult fitResult = session.getHighTFit();
    if (fitResult != null && !activeModelTerms.isEmpty()) {
      try {
        pctRMS = fitResult.pctRMS();
        rmsLabel.setText("RMS: " + pctRMS);
        iterations = fitResult.iterations();
        iterLabel.setText("Iter: " + iterations);
        int pointsCount = 200;

        double[] fitX = new double[pointsCount];
        double[] fitY;
        double step = (maxT - minT) / (pointsCount - 1);

        HighTSpecificHeatModel.HighTFitModel[] modelArray =
            activeModelTerms.toArray(new HighTSpecificHeatModel.HighTFitModel[0]);
        HighTSpecificHeatModel highTModel = new HighTSpecificHeatModel(modelArray);

        for (int i = 0; i < pointsCount; i++) {
          fitX[i] = minT + i * step;
        }

        fitY = highTModel.value(fitX, fitResult.coefficients());

        dataset.addSeries("Fit", new double[][] {fitX, fitY});

        if (rawData != null) {
          if (rawData.temperatures() != null) {
            double[][] deviationsSet = getDeviations(highTModel, fitResult.coefficients(), rawData);

            deviations.addSeries("Deviations", deviationsSet);
          }
        }

      } catch (NumberFormatException _) {

      }
    }

    highTGraph.getXYPlot().setDataset(this.dataset);
    devGraph.getXYPlot().setDataset(deviations);
    this.revalidate();
    this.repaint();
  }

  private HeatCapacityData getHighTData(AnalysisSession session, double minT, double maxT) {
    HeatCapacityData data = session.getRawData();

    int minIdx =
        IntStream.range(0, data.temperatures().length)
            .filter(i -> data.temperatures()[i] >= minT)
            .findFirst()
            .orElse(0);
    int maxIdx =
        IntStream.range(0, data.temperatures().length)
            .filter(i -> data.temperatures()[i] <= maxT)
            .reduce((_, second) -> second)
            .orElse(data.temperatures().length - 1);

    double[] highT = Arrays.copyOfRange(data.temperatures(), minIdx, maxIdx + 1);
    double[] highHC = Arrays.copyOfRange(data.heatCapacities(), minIdx, maxIdx + 1);

    return new HeatCapacityData(highT, highHC);
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if ("rawData".equals(event.getPropertyName())) {
      updateChart();
    }
  }
}
