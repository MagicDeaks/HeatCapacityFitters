package com.magicdeaks.heatcapacity.frames;

import com.magicdeaks.heatcapacity.session.AnalysisSession;
import com.magicdeaks.heatcapacity.tabs.DataImportTab;
import com.magicdeaks.heatcapacity.tabs.HighTFitTab;
import com.magicdeaks.heatcapacity.tabs.LowTFitTab;
import com.magicdeaks.heatcapacity.tabs.MidTFitTab;
import com.magicdeaks.heatcapacity.tabs.ThermCalcTab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainFrame {
    private JFrame frame;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private AnalysisSession session;

    private static final String DATA_IMPORT = "Import";
    private static final String LOW_T = "Low T";
    private static final String MID_T = "Mid T";
    private static final String HIGH_T = "High T";
    private static final String THERM_CALC = "Therm Calc";
    private static final String RESULTS = "Results";

    public MainFrame() {
        session = new AnalysisSession();
        initialize();
    }

    public void initialize() {
        frame = new JFrame();
        frame.setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu fitMenu = new JMenu("Fitting");
        JMenu calcMenu = new JMenu("Calculate");

        menuBar.add(fileMenu);
        menuBar.add(fitMenu);
        menuBar.add(calcMenu);

        frame.setJMenuBar(menuBar);

        addView(new DataImportTab(session), DATA_IMPORT, fileMenu);
        addView(new LowTFitTab(session), LOW_T, fitMenu);
        addView(new MidTFitTab(session), MID_T, fitMenu);
        addView(new HighTFitTab(session), HIGH_T, fitMenu);
        addView(new ThermCalcTab(session), THERM_CALC, calcMenu);
        
        JPanel resultsTab = new JPanel();
        resultsTab.add(new JLabel("Results"));
        addView(resultsTab, RESULTS, calcMenu);

        frame.add(cardPanel, BorderLayout.CENTER);

        frame.setTitle("Heat Capacity Fitter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
    }

    private void addView(Component component, String identifier, JMenu menu) {
        cardPanel.add(component, identifier);

        JMenuItem menuItem = new JMenuItem(identifier);

        menuItem.addActionListener((ActionEvent _) -> cardLayout.show(cardPanel, identifier));

        menu.add(menuItem);
    }

    public void show() {
        frame.setVisible(true);
    }

    private final Color colour1 =  new Color(0, 0, 0);
    private final Color colour2 = new Color(20, 33, 61);
    private final Color colour3 = new Color(252, 163, 17);
    private final Color colour4 = new Color(229, 229, 229);
    private final Color colour5 = new Color(255, 255, 255);

}
