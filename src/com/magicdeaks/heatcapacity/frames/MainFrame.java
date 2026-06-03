package com.magicdeaks.heatcapacity.frames;

import com.magicdeaks.heatcapacity.HeatCapacityData;
import com.magicdeaks.heatcapacity.util.DataProcesser;
import com.magicdeaks.heatcapacity.util.DataReader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.Optional;

public class MainFrame {
    private JFrame frame;
    private JTextField copperMassField;
    private JTextField formulaField;

    private String copperInput;
    private String formulaInput;

    private boolean subtractCopper = true;

    private final Color colour1 =  new Color(0, 0, 0);
    private final Color colour2 = new Color(20, 33, 61);
    private final Color colour3 = new Color(252, 163, 17);
    private final Color colour4 = new Color(229, 229, 229);
    private final Color colour5 = new Color(255, 255, 255);

    public HeatCapacityData data;

    public MainFrame() {
        initialize();
    }

    public void initialize() {
        frame = new JFrame();
        frame.setLayout(new BorderLayout());

        JPanel centrePanel = new JPanel();

        JPanel rightPanel = new JPanel();
        JPanel topRightPanel = new JPanel();
        JPanel bottomRightPanel = new JPanel();
        JPanel rightTopRightPanel = new JPanel();
        JPanel leftTopRightPanel = new JPanel();
        JPanel topLeftTopRightPanel = new JPanel();
        JPanel bottomLeftTopRightPanel = new JPanel();

        rightPanel.setLayout(new BorderLayout());

        topRightPanel.setLayout(new BorderLayout());
        bottomRightPanel.setLayout(new BorderLayout());

        rightTopRightPanel.setLayout(new BorderLayout());
        leftTopRightPanel.setLayout(new BorderLayout());

        topLeftTopRightPanel.setLayout(new BorderLayout());
        bottomLeftTopRightPanel.setLayout(new BorderLayout());


        rightPanel.add(topRightPanel, BorderLayout.NORTH);
        rightPanel.add(bottomRightPanel, BorderLayout.SOUTH);

        topRightPanel.add(rightTopRightPanel, BorderLayout.EAST);
        topRightPanel.add(leftTopRightPanel, BorderLayout.WEST);

        leftTopRightPanel.add(topLeftTopRightPanel, BorderLayout.NORTH);
        leftTopRightPanel.add(bottomLeftTopRightPanel, BorderLayout.SOUTH);

        centrePanel.setBackground(colour1);
        rightPanel.setBackground(colour2);
        topRightPanel.setBackground(colour3);
        bottomRightPanel.setBackground(colour3);
        rightTopRightPanel.setBackground(colour4);
        leftTopRightPanel.setBackground(colour4);
        topLeftTopRightPanel.setBackground(colour5);
        bottomLeftTopRightPanel.setBackground(colour5);

        copperMassField = new JTextField(10);
        topLeftTopRightPanel.add(copperMassField, BorderLayout.NORTH);
        ActionListener copperListener = _ -> {
            copperInput = copperMassField.getText();
            System.out.println(copperInput);
        };
        JButton copperButton = createButton("Copper (mg)", copperListener);
        topLeftTopRightPanel.add(copperButton, BorderLayout.SOUTH);

        formulaField = new JTextField(10);
        rightTopRightPanel.add(formulaField, BorderLayout.NORTH);
        ActionListener formulaListener = _ -> {
            formulaInput = formulaField.getText();
            System.out.println(formulaInput);
        };
        JButton formulaButton = createButton("Formula", formulaListener);
        rightTopRightPanel.add(formulaButton, BorderLayout.SOUTH);

        JCheckBox copperCheckBox = new JCheckBox("Copper Sub", true);
        copperCheckBox.addItemListener(event -> subtractCopper = event.getStateChange() == ItemEvent.SELECTED);
        bottomLeftTopRightPanel.add(copperCheckBox);

        JButton calculateCpButton = createButton("Calculate CP", _ -> {
            double molecularWeight = DataProcesser.getMolecularWeight(getFormulaInput().orElse(" "));
            data = DataReader.readDAT("data.dat");
            if (subtractCopper) {
                data = DataProcesser.subtractCopper(data, getCopperInput());
            }

            data = DataProcesser.scaleHeatCapacity(data, molecularWeight);
        });
        bottomRightPanel.add(calculateCpButton, BorderLayout.NORTH);

        frame.add(rightPanel, BorderLayout.EAST);
        frame.add(centrePanel, BorderLayout.CENTER);

        frame.setTitle("Heat Capacity Fitter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
    }

    public void show() {
        frame.setVisible(true);
    }

    private JButton createButton(String name, ActionListener listener) {
        JButton button = new JButton(name);
        button.addActionListener(listener);
        return button;
    }

    private JButton createButton(String name, ImageIcon icon, ActionListener listener) {
        JButton button = new JButton(name, icon);
        button.addActionListener(listener);
        return button;
    }

    public double getCopperInput(){
        try {
            return Double.parseDouble(copperInput);
        } catch (NumberFormatException | NullPointerException e) {
            return 0.0;
        }
    }

    public Optional<String> getFormulaInput(){
        return Optional.ofNullable(formulaInput);
    }
}
