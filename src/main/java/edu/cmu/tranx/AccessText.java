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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 *
 */
public class AccessText extends AnAction {

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

        System.out.println(document.getCharsSequence());

        final int start = selectionModel.getSelectionStart();
        final int end = selectionModel.getSelectionEnd();

        //Access document, caret, and selection
        try {
            ArrayList<TranXHttpClient.Hypothesis> options = TranXHttpClient.sendData(query).hypotheses;
            System.out.print(query);
            BaseListPopupStep<TranXHttpClient.Hypothesis> q_list = new BaseListPopupStep<>
                    ("You searched" + " for: " + query + " here is a list of results", options) {
                @Override
                public String getTextFor(TranXHttpClient.Hypothesis value) {
                    return "id: " + value.id + "\t" + "score: " + value.score + "\n" +
                            "snippet: " + value.value;
                }

                @Override
                public PopupStep onChosen(TranXHttpClient.Hypothesis selectedValue, boolean finalChoice) {
                    final Runnable runnable = () -> document.replaceString(start, end,
                            selectedValue.value
                    );
                    WriteCommandAction.runWriteCommandAction(project, runnable);
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
        e.getPresentation().setVisible((project != null && editor != null  ));
    }
}

