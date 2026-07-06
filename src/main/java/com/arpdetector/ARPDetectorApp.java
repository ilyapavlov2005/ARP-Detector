package com.arpdetector;

import javax.swing.*;

public class ARPDetectorApp {

    public static void main(String[] args) {
        try {
            // Устанавливаем Metal Look and Feel (самый надёжный)
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }}