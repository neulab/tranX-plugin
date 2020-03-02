package edu.cmu.tranx;

import javax.swing.*;

public class TranXConfigurableGUI {
    private JTextField textField1;
    private JPanel rootPanel;
    private JLabel label;
    private TranXConfig config;

    TranXConfigurableGUI() {

    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public void createUI() {
        config = TranXConfig.getInstance();
        textField1.setText(config.getUserName());
    }

    public boolean isModified() {
        boolean modified;
        modified = !textField1.getText().equals(config.getUserName());
        return modified;
    }

    public void apply() {
        config.setUserName(textField1.getText());
    }

    public void reset() {
        textField1.setText(config.getUserName());
    }

}
