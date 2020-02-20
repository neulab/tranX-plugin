package edu.cmu.tranx;

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

import edu.cmu.tranx.UploadHttpClient;

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
            ArrayList<Hypothesis> options = TranXHttpClient.sendData(query).hypotheses;
            ArrayList<Hypothesis> stackOverflowOptions = StackOverflowClient.sendData(query).hypotheses;
            options.addAll(stackOverflowOptions);

            BaseListPopupStep<Hypothesis> q_list = new BaseListPopupStep<>
                    ("You searched for: '" + query + "', here is a list of results:", options) {
                @Override
                public String getTextFor(Hypothesis value) {
                    if (value.id > 0)
                        return "GEN: " + value.value;
                    else
                        return "RET: " + value.value;
                }

                @Override
                public PopupStep onChosen(Hypothesis selectedValue, boolean finalChoice) {
                    String toInsert =
                            "# ---- BEGIN AUTO-GENERATED CODE ----\n" + indent +
                            "# to remove these comments and send feedback press alt-G\n" + indent +
                            selectedValue.value + "\n"  + indent +
                            "# ---- END AUTO-GENERATED CODE ----\n";
                    final Runnable runnable = () -> document.replaceString(start, end, toInsert);
                    WriteCommandAction.runWriteCommandAction(project, runnable);
                    int selectedIndex = options.indexOf(selectedValue);

                    if (!UploadHttpClient.sendQueryData(query, config.getUserName(),
                            selectedIndex, options, document.getText()))
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

