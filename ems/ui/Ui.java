package ems.ui;

import javax.swing.*;

public final class Ui {
    private Ui() {}

    public static void info(java.awt.Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "EMS", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(java.awt.Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "EMS - Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(java.awt.Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "EMS", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}