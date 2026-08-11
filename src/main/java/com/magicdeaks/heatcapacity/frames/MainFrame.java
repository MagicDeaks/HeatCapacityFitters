package com.magicdeaks.heatcapacity.frames;

import com.magicdeaks.heatcapacity.io.FileSystem;
import com.magicdeaks.heatcapacity.session.AnalysisSession;
import com.magicdeaks.heatcapacity.tabs.DataImportTab;
import com.magicdeaks.heatcapacity.tabs.HighTFitTab;
import com.magicdeaks.heatcapacity.tabs.LowTFitTab;
import com.magicdeaks.heatcapacity.tabs.MidTFitTab;
import com.magicdeaks.heatcapacity.tabs.ThermCalcTab;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainFrame {
    private JFrame frame;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private AnalysisSession SESSION;

    private String savePath;

    private static final String DATA_IMPORT = "Import";
    private static final String LOW_T = "Low T";
    private static final String MID_T = "Mid T";
    private static final String HIGH_T = "High T";
    private static final String THERM_CALC = "Therm Calc";
    private static final String RESULTS = "Results";

    private DataImportTab dataImportTab;
    private LowTFitTab lowTFitTab;
    private MidTFitTab midTFitTab;
    private HighTFitTab highTFitTab;
    private ThermCalcTab thermCalcTab;

    public MainFrame() {
        SESSION = new AnalysisSession();
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

        dataImportTab = new DataImportTab(SESSION);
        lowTFitTab = new LowTFitTab(SESSION);
        midTFitTab = new MidTFitTab(SESSION);
        highTFitTab = new HighTFitTab(SESSION);
        thermCalcTab = new ThermCalcTab(SESSION);

        addView(dataImportTab, DATA_IMPORT, fileMenu);
        addView(lowTFitTab, LOW_T, fitMenu);
        addView(midTFitTab, MID_T, fitMenu);
        addView(highTFitTab, HIGH_T, fitMenu);
        addView(thermCalcTab, THERM_CALC, calcMenu);

        JPanel resultsTab = new JPanel();
        resultsTab.add(new JLabel("Results"));
        addView(resultsTab, RESULTS, calcMenu);

        JMenuItem openItem = new JMenuItem("Open");
        fileMenu.add(openItem);

        openItem.addActionListener(_ -> open());

        JMenuItem saveAsItem = new JMenuItem("Save As");
        fileMenu.add(saveAsItem);

        saveAsItem.addActionListener(_ -> saveAs());

        JMenuItem saveItem = new JMenuItem("Save");
        fileMenu.add(saveItem);

        saveItem.addActionListener(
                _ -> {
                    if (savePath != null) {
                        FileSystem.writeFile(SESSION, savePath);
                    } else {
                        saveAs();
                    }
                });

        saveItem.setAccelerator(KeyStroke.getKeyStroke("alt S"));

        JMenuItem exitItem = new JMenuItem("Exit");
        fileMenu.add(exitItem);

        exitItem.addActionListener(_ -> System.exit(0));

        frame.add(cardPanel, BorderLayout.CENTER);

        frame.setTitle("Heat Capacity Fitter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null);
    }

    private void saveAs() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select save file");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("WTF Files (*.wtf)", "wtf");
        fileChooser.setFileFilter(filter);

        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();

            if (!filePath.toLowerCase().endsWith(".wtf")) {
                filePath = filePath + ".wtf";
            }

            FileSystem.writeFile(SESSION, filePath);
            savePath = filePath;
        } else if (userSelection == JFileChooser.CANCEL_OPTION) {
            System.out.println("Save cancelled by user.");
        }
    }

    private void open() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select file");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("WTF File (*.wtf)", "wtf");
        fileChooser.setFileFilter(filter);

        fileChooser.setCurrentDirectory(new File(System.getProperty("wser.dir")));
        int userSelection = fileChooser.showOpenDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();

            if (!filePath.toLowerCase().endsWith(".wtf")) {
                System.out.println("Wrong file type.");
                return;
            }

            FileSystem.readFile(filePath);
            savePath = filePath;

            update();
        } else if (userSelection == JFileChooser.CANCEL_OPTION) {
            System.out.println("Open cancelled by user.");
        }
    }

    private void update() {
        dataImportTab.updateSession();
        lowTFitTab.updateSession();
        midTFitTab.updateSession();
        // highTFitTab.updateSession();
        // thermCalcTab.updateSession();
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

    private final Color colour1 = new Color(0, 0, 0);
    private final Color colour2 = new Color(20, 33, 61);
    private final Color colour3 = new Color(252, 163, 17);
    private final Color colour4 = new Color(229, 229, 229);
    private final Color colour5 = new Color(255, 255, 255);
}
