package com.magicdeaks.heatcapacity.frames;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainFrame {
    JFrame frame;

    public MainFrame() {
        initialize();
    }

    public void initialize() {
        frame = new JFrame();
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBackground(Color.RED);

        frame.add(panel, BorderLayout.CENTER);

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
}
