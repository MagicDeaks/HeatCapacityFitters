package com.magicdeaks.heatcapacity.frames;

import com.magicdeaks.heatcapacity.session.AnalysisSession;
import com.magicdeaks.heatcapacity.tabs.DataImportTab;
import com.magicdeaks.heatcapacity.tabs.HighTFitTab;
import com.magicdeaks.heatcapacity.tabs.LowTFitTab;
import com.magicdeaks.heatcapacity.tabs.MidTFitTab;

import javax.swing.*;
import java.awt.*;

public class MainFrame {
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private AnalysisSession session;

    public MainFrame() {
        session = new AnalysisSession();
        initialize();
    }

    public void initialize() {
        frame = new JFrame();
        frame.setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        DataImportTab dataTab = new DataImportTab(session);
        tabbedPane.addTab("Data Import", dataTab);

        JPanel lowTTab = new LowTFitTab(session);
        tabbedPane.addTab("Low T", lowTTab);

        JPanel midTTab = new MidTFitTab(session);
        tabbedPane.addTab("Mid T", midTTab);

        JPanel highTTab = new HighTFitTab(session);
        tabbedPane.addTab("High T", highTTab);

        JPanel thermCalcTab = new JPanel();
        thermCalcTab.add(new JLabel("Calculate Thermodynamic Functions"));
        tabbedPane.addTab("Therm Calc", thermCalcTab);

        JPanel resultsTab = new JPanel();
        resultsTab.add(new JLabel("Results & Export"));
        tabbedPane.addTab("Results", resultsTab);

        frame.add(tabbedPane, BorderLayout.CENTER);

        frame.setTitle("Heat Capacity Fitter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
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
