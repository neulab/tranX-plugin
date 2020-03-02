package edu.cmu.tranx;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.DocumentUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 *
 */
public class QueryIntent extends AnAction {
    private final TranXConfig config = TranXConfig.getInstance();
    private final CodeSyntaxHighlighter highlighter = CodeSyntaxHighlighter.getInstance();

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
        if (Utils.isBlankString(query)) return;
        final Document document = editor.getDocument();
        final SelectionModel selectionModel = editor.getSelectionModel();

        int start = selectionModel.getSelectionStart();
        int end = selectionModel.getSelectionEnd();
        int lineStartOffset = DocumentUtil.getLineStartOffset(start, document);
        String indent = document.getText(new TextRange(lineStartOffset, start));
        try {
            CompletableFuture<List<Hypothesis>> tranXCandidatesFuture = CompletableFuture.supplyAsync(
                    () -> TranXHttpClient.getCandidates(query));

            CompletableFuture<List<Hypothesis>> stackOverflowCandidatesFuture = CompletableFuture.supplyAsync(
                    () -> StackOverflowClient.getCandidates(query));

            CompletableFuture<List<Hypothesis>> combinedCandidatesFuture = tranXCandidatesFuture
                    .thenCombine(stackOverflowCandidatesFuture, (tranXCandidates, stackOverflowCandidates) -> {
                        List<Hypothesis> candidates = new ArrayList<>();
                        if (tranXCandidates != null || stackOverflowCandidates != null) {
                            if (tranXCandidates == null) {
                                HintManager.getInstance().showErrorHint(editor, "Error: TranX query failed!");
                            } else {
                                candidates.addAll(Utils.firstK(tranXCandidates, 7));
                            }
                            if (stackOverflowCandidates == null) {
                                HintManager.getInstance().showErrorHint(editor, "Error: StackOverflow query failed!");
                            } else {
                                candidates.addAll(Utils.firstK(stackOverflowCandidates, 7));
                            }
                        }
                        return candidates;
                    });

            List<Hypothesis> options = combinedCandidatesFuture.get();
            getCodeHtml(options);

            BaseListPopupStep<Hypothesis> qList = new BaseListPopupStep<>
                    ("You searched for: '" + query + "', here is a list of results:", options) {

                @NotNull
                @Override
                public String getTextFor(Hypothesis value) {
                    return value.htmlValue;
                }

                @Override
                public Icon getIconFor(Hypothesis value) {
                    if (value.id > 0)
                        return AllIcons.General.BalloonInformation;
                    else
                        return AllIcons.General.Web;
                }


                @Override
                public PopupStep onChosen(Hypothesis selectedValue, boolean finalChoice) {
                    String hash = HashStringGenerator.generateHashString();
                    int selectedIndex = options.indexOf(selectedValue);
                    String toInsert =
                            "# ---- BEGIN AUTO-GENERATED CODE ----\n" +
                                    "# ---- " + hash + " ----\n" +
                                    "# query: " + query + "\n" +
                                    "# to remove these comments and send feedback press alt-G\n" +
                                    selectedValue.value + "\n" +
                                    "# ---- END AUTO-GENERATED CODE ----\n";
                    String finalToInsert = Utils.insertIndent(toInsert, indent);
                    final Runnable runnable = () -> document.replaceString(start, end, finalToInsert);
                    WriteCommandAction.runWriteCommandAction(project, runnable);

                    if (!UploadHttpClient.sendQueryData(query, config.getUserName(),
                            selectedIndex, options, document.getText(), hash)) {
                        UndoManager.getInstance(project).undo(FileEditorManager.getInstance(project).getSelectedEditor());
                        HintManager.getInstance().showErrorHint(editor, "Error: Upload failed.");
                    }
                    return super.onChosen(selectedValue, finalChoice);
                }

            };

            JBPopupFactory jbPopupFactory = JBPopupFactory.getInstance();
            jbPopupFactory.createListPopup(qList).show(jbPopupFactory.guessBestPopupLocation(editor));
            selectionModel.removeSelection();
        } catch(Exception e) {
            String exMsg = "Caught exception: " + e;
            System.err.println(exMsg);
            HintManager.getInstance().showErrorHint(editor, exMsg);
        }
    }

    private void getCodeHtml(List<Hypothesis> options) {
        List<String> codeList = new ArrayList<>();
        for (Hypothesis option : options) {
            codeList.add(option.value);
        }

        List<String> htmlTextList = highlighter.highlightCodeList(codeList);
        assert htmlTextList.size() == options.size();

        for (int i = 0; i < htmlTextList.size(); i++) {
            options.get(i).htmlValue = htmlTextList.get(i);
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

