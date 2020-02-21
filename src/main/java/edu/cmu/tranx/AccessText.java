package edu.cmu.tranx;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.DocumentUtil;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class AccessText extends AnAction {
    private final TranXConfig config = TranXConfig.getInstance();

    @Override
    public void actionPerformed(final AnActionEvent anActionEvent) {
        //Get all the required data from data keys
        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);

        // ensure userId set in settings
        if (config == null || config.getUserName() == null || config.getUserName().equals("")) {
            HintManager.getInstance().showErrorHint(editor, "Error: UserID not set in plugin settings");
            return;
        }

        TextInputForm form = new TextInputForm();
        JBPopupFactory jbPopupFactory = JBPopupFactory.getInstance();
        ComponentPopupBuilder popupBuilder = jbPopupFactory.createComponentPopupBuilder(form.rootPanel, null);
        JBPopup popup = popupBuilder.createPopup();
        popup.setRequestFocus(true);
        popup.show(jbPopupFactory.guessBestPopupLocation(editor));
        form.textField1.requestFocus();
        ActionListener al = e -> {
            popup.closeOk(null);
            displayResults(form.textField1.getText(), editor, project);
        };
        form.OKButton.addActionListener(al);
        form.textField1.addActionListener(al);
    }

    private void displayResults(String query, Editor editor, Project project) {
        final Document document = editor.getDocument();
        final SelectionModel selectionModel = editor.getSelectionModel();

        int start = selectionModel.getSelectionStart();
        int end = selectionModel.getSelectionEnd();
        int lineStartOffset = DocumentUtil.getLineStartOffset(start, document);
        String indent = document.getText(new TextRange(lineStartOffset, start));
        //Access document, caret, and selection
        try {
            List<Hypothesis> options = TranXHttpClient.sendData(query).hypotheses;
            options = Utils.firstK(options, 7);
            List<Hypothesis> stackOverflowOptions = StackOverflowClient.sendData(query).hypotheses;
            stackOverflowOptions = Utils.firstK(stackOverflowOptions, 7);
            options.addAll(stackOverflowOptions);

            List<Hypothesis> finalOptions = options;

            BaseListPopupStep<Hypothesis> q_list = new BaseListPopupStep<>
                    ("You searched for: '" + query + "', here is a list of results:", finalOptions) {
                @Override
                public String getTextFor(Hypothesis value) {
                    if (value.id > 0)
                        return "GEN: " + value.value;
                    else
                        return "RET: " + value.value;
                }

                @Override
                public PopupStep onChosen(Hypothesis selectedValue, boolean finalChoice) {
                    String hash = HashStringGenerator.generateHashString();
                    String toInsert =
                            "# ---- BEGIN AUTO-GENERATED CODE ----\n" + indent +
                            "# ---- " + hash + " ----\n" + indent +
                            "# to remove these comments and send feedback press alt-G\n" + indent +
                            selectedValue.value + "\n"  + indent +
                            "# ---- END AUTO-GENERATED CODE ----\n";
                    final Runnable runnable = () -> document.replaceString(start, end, toInsert);
                    WriteCommandAction.runWriteCommandAction(project, runnable);
                    int selectedIndex = finalOptions.indexOf(selectedValue);

                    if (!UploadHttpClient.sendQueryData(query, config.getUserName(),
                            selectedIndex, finalOptions, document.getText(), hash))
                        System.out.println("QUERY UPLOAD ERROR!");
                    return super.onChosen(selectedValue, finalChoice);
                }

            };

            JBPopupFactory jbPopupFactory = JBPopupFactory.getInstance();
            jbPopupFactory.createListPopup(q_list).show(jbPopupFactory.guessBestPopupLocation(editor));

            selectionModel.removeSelection();
        } catch(Exception e) {
            System.err.println("Caught exception " + e);
        }
    }


    @Override
    public void update(final AnActionEvent e) {
        //Get required data keys
        final Project project = e.getData(CommonDataKeys.PROJECT);
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        //Set visibility only in case of existing project and editor and if some text in the editor is selected
        e.getPresentation().setVisible((project != null && editor != null));
    }
}

