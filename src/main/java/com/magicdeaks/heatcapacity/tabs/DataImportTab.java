package com.magicdeaks.heatcapacity.tabs;

import com.magicdeaks.heatcapacity.records.HeatCapacityData;
import com.magicdeaks.heatcapacity.session.AnalysisSession;
import com.magicdeaks.heatcapacity.util.DataProcesser;
import com.magicdeaks.heatcapacity.util.DataReader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.Optional;

public class DataImportTab extends JPanel {
    private final AnalysisSession SESSION;

    private JTextField filePathField;
    private JButton browseButton;
    private File selectedDataFile;

    private JTextField formulaField;
    private JTextField massField;
    private JTextField copperMassField;
    private JTextField atomsField;
    private JCheckBox copperCheckBox;
    private JButton calculateCpButton;
    private JLabel statusLabel;

    private double molecularWeight;

    private boolean subtractCopper = true;

    public DataImportTab(AnalysisSession SESSION) {
        this.SESSION = SESSION;
        initializeUI();
    }

    private void initializeUI() {
        filePathField = new JTextField(20);
        filePathField.setEditable(false);
        browseButton = new JButton("Browse...");

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Heat Capacity Data File");

            FileNameExtensionFilter datFilter = new FileNameExtensionFilter("DAT Data Files (*.dat)", "dat");

            fileChooser.setFileFilter(datFilter);

            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

            int userSelection = fileChooser.showOpenDialog(DataImportTab.this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                selectedDataFile = fileChooser.getSelectedFile();
                filePathField.setText(selectedDataFile.getAbsolutePath());
            }
        });

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(browseButton, gbc);
        gbc.gridx = 1;
        add(filePathField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Formula:"), gbc);
        formulaField = new JTextField(15);
        gbc.gridx = 1;
        add(formulaField, gbc);

        gbc.gridx = 2;
        atomsField = new JTextField(5);
        atomsField.setToolTipText("Enter Atoms per Formula Unit");
        add(atomsField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Sample Mass (mg):"), gbc);
        massField = new JTextField("10.0", 15);
        gbc.gridx = 1;
        add(massField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        copperCheckBox = new JCheckBox("Subtract Copper", subtractCopper);
        copperCheckBox.addItemListener(e -> subtractCopper = e.getStateChange() == ItemEvent.SELECTED);
        add(copperCheckBox, gbc);

        copperMassField = new JTextField(15);
        copperMassField.setToolTipText("Enter Copper Mass in mg");
        gbc.gridx = 1;
        add(copperMassField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        calculateCpButton = new JButton("Load and Calculate Cp");
        add(calculateCpButton, gbc);

        gbc.gridy = 5;
        statusLabel = new JLabel("Status: Waiting for data...");
        add(statusLabel, gbc);

        calculateCpButton.addActionListener(_ -> processDataInBackground());
    }

    private void processDataInBackground() {
        if (selectedDataFile == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a data file.",
                    "Missing File",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (massField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter the sample mass",
                    "Missing Input",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        calculateCpButton.setEnabled(false);
        statusLabel.setText("Status: Processing data...");

        final String FORMULA = getFormulaInput().orElse(" ");
        final double SAMPLE_MASS = getDoubleFromField(massField, 10.0);
        final double COPPER_MASS = getDoubleFromField(copperMassField, 0.0);
        final boolean COPPER_SUB = subtractCopper;
        final double ATOMS = getDoubleFromField(atomsField, 0.0);

        SwingWorker<HeatCapacityData, Void> worker = new SwingWorker<>() {
            @Override
            protected HeatCapacityData doInBackground() throws Exception {
                molecularWeight = DataProcesser.getMolecularWeight(FORMULA);

                HeatCapacityData parsedData = DataReader.readDAT(selectedDataFile.getAbsolutePath());

                if (COPPER_SUB && COPPER_MASS > 0) {
                    parsedData = DataProcesser.subtractCopper(parsedData, COPPER_MASS);
                }

                return DataProcesser.scaleHeatCapacity(parsedData, SAMPLE_MASS, molecularWeight);
            }

            @Override
            protected void done() {
                try {
                    HeatCapacityData finalData = get();

                    SESSION.setRawData(finalData);
                    SESSION.setAtoms(ATOMS);
                    SESSION.setMolecularWeight(molecularWeight);

                    statusLabel.setText("Status: Data loaded successfully! (" + finalData.temperatures().length + " points)");
                } catch (Exception e) {
                    statusLabel.setText("Status: Error processing data.");
                    JOptionPane.showMessageDialog(DataImportTab.this,
                            "Error processing data: " + e.getMessage(),
                            "Calculating Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    calculateCpButton.setEnabled(true);
                    browseButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private double getDoubleFromField(JTextField field, double defaultValue) {
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }

    private Optional<String> getFormulaInput() {
        String text = formulaField.getText().trim();
        return text.isEmpty() ? Optional.empty() : Optional.of(text);
    }
}
