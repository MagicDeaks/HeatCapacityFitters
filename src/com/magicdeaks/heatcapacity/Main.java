package com.magicdeaks.heatcapacity;

import com.magicdeaks.heatcapacity.frames.MainFrame;

import javax.swing.*;

public class Main {
    static void main() {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.show();
        });
    }
}