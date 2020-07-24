package edu.cmu.tranx;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

@State(
        name = "TranXConfig",
        storages = {
                @Storage("TranXConfig.xml")}
)
public class TranXConfig implements PersistentStateComponent<TranXConfig> {

    String userName;
    boolean enableQuery;
    boolean enableFineGrainedEdit;

    // default state of the component (one used if there is nothing persisted in the XML files yet).
    TranXConfig() {
        userName = "";
        enableQuery = true;
        enableFineGrainedEdit = false;
    }

    public boolean isEnableFineGrainedEdit() {
        return enableFineGrainedEdit;
    }

    public void setEnableFineGrainedEdit(boolean enableFineGrainedEdit) {
        this.enableFineGrainedEdit = enableFineGrainedEdit;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isEnableQuery() {
        return enableQuery;
    }

    public void setEnableQuery(boolean enableQuery) {
        this.enableQuery = enableQuery;
    }


    @Nullable
    @Override
    public TranXConfig getState() {
        return this;
    }

    @Override
    public void loadState(TranXConfig singleFileExecutionConfig) {
        XmlSerializerUtil.copyBean(singleFileExecutionConfig, this);
    }

    @Nullable
    public static TranXConfig getInstance() {
        return ServiceManager.getService(TranXConfig.class);
    }

}
