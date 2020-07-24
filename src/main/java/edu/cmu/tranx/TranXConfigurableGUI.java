package edu.cmu.tranx;

import javax.swing.*;

public class TranXConfigurableGUI {
    private JTextField textField1;
    private JPanel rootPanel;
    private JCheckBox enableQueryCheckBox;
    private JCheckBox enableFineGrainedEditCheckBox;
    private TranXConfig config;

    TranXConfigurableGUI() {

    }

    public JPanel getRootPanel() {
        return rootPanel;
    }

    public void createUI() {
        config = TranXConfig.getInstance();
        textField1.setText(config.getUserName());
        enableQueryCheckBox.setSelected(config.isEnableQuery());
        enableFineGrainedEditCheckBox.setSelected(config.isEnableFineGrainedEdit());
    }

    public boolean isModified() {
        boolean modified;
        modified = !textField1.getText().equals(config.getUserName()) ||
                !enableQueryCheckBox.isSelected() == config.isEnableQuery() ||
                !enableFineGrainedEditCheckBox.isSelected() == config.isEnableFineGrainedEdit();
        return modified;
    }

    public void apply() {
        config.setUserName(textField1.getText());
        config.setEnableQuery(enableQueryCheckBox.isSelected());
        config.setEnableFineGrainedEdit(enableFineGrainedEditCheckBox.isSelected());
    }

    public void reset() {
        textField1.setText(config.getUserName());
        enableQueryCheckBox.setSelected(config.isEnableQuery());
        enableFineGrainedEditCheckBox.setSelected(config.isEnableFineGrainedEdit());
    }

}
