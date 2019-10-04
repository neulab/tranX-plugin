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

        TextInputForm form = new TextInputForm();
        JBPopupFactory jbPopupFactory = JBPopupFactory.getInstance();
        ComponentPopupBuilder popupBuilder = jbPopupFactory.createComponentPopupBuilder(form.rootPanel, null);
        JBPopup popup = popupBuilder.createPopup();
        popup.setRequestFocus(true);
        popup.show(jbPopupFactory.guessBestPopupLocation(editor));
        form.textField1.requestFocus();
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                popup.closeOk(null);
                displayResults(form.textField1.getText(), anActionEvent);
            }
        };
        form.OKButton.addActionListener(al);
        form.textField1.addActionListener(al);
    }
    private void displayResults(String query, final AnActionEvent anActionEvent) {
        //Access document, caret, and selection
        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);
        final Document document = editor.getDocument();
        final SelectionModel selectionModel = editor.getSelectionModel();
        final int start = selectionModel.getSelectionStart();
        final int end = selectionModel.getSelectionEnd();
        try {
            ArrayList<HttpClient.Hypothesis> options = HttpClient.sendData(query).hypotheses;
            System.out.print(query);
            BaseListPopupStep<HttpClient.Hypothesis> q_list = new BaseListPopupStep<HttpClient.Hypothesis>
                    ("You searched" + " for: " + query + " here is a list of results", options) {
                @Override
                public String getTextFor(HttpClient.Hypothesis value) {
                    return "id: " + value.id + "\t" + "score: " + value.score + "\n" +
                            "snippet: " + value.value;
                }

                @Override
                public PopupStep onChosen(HttpClient.Hypothesis selectedValue, boolean finalChoice) {
                    final Runnable runnable = () -> document.replaceString(start, end,
                            // "# ---- BEGIN AUTO-GENERATED CODE ----\n" +
                            // "# to remove these comments and send feedback press alt-G\n" +
                            //         selectedValue.value +
                            // "\n# ---- END AUTO-GENERATED CODE ----\n"
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

