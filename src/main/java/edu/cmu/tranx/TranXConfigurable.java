package edu.cmu.tranx;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TranXConfigurable implements SearchableConfigurable {
    TranXConfigurableGUI gui;
    private final TranXConfig config;

    public TranXConfigurable() {
        config = TranXConfig.getInstance();
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "TranX Plugin";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "preference.TranXConfigurable";
    }

    @NotNull
    @Override
    public String getId() {
        return "preference.TranXConfigurable";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String s) {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        gui = new TranXConfigurableGUI();
        gui.createUI();
        return gui.getRootPanel();
    }

    @Override
    public boolean isModified() {
        return gui.isModified();
    }

    @Override
    public void apply() throws ConfigurationException {
        gui.apply();
    }

    @Override
    public void reset() {
        gui.reset();
    }

    @Override
    public void disposeUIResources() {
        gui = null;
    }

}
