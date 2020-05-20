package edu.cmu.tranx;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class GetEdit extends AnAction {
    private static final String GEN_PATTERN =
            "(?s)# ---- BEGIN AUTO-GENERATED CODE ----\n\\s*# ---- ([a-z0-9]+) ----\n\\s*# query: (.*)\n\\s*# to remove these comments and send feedback press alt-G\\s(.*?)\n\\s*# ---- END AUTO-GENERATED CODE ----";

    private Pattern genPattern = Pattern.compile(GEN_PATTERN);
    private final TranXConfig config = TranXConfig.getInstance();


    @Override
    public void actionPerformed(final AnActionEvent anActionEvent) {
        //Get all the required data from data keys
        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);
        final Document document = editor.getDocument();
        final VirtualFile file = FileDocumentManager.getInstance().getFile(document);

        final SelectionModel selectionModel = editor.getSelectionModel();

        // ensure userId set in settings
        if (config == null || config.getUserName() == null || config.getUserName().equals("")) {
            HintManager.getInstance().showErrorHint(editor, "Error: UserID not set in plugin settings");
            return;
        }

        int start = selectionModel.getSelectionStart();

        String sourceCode = document.getText();

        int matchedStart, matchedEnd;
        String modifiedCode, hash, query;
        Matcher matcher = genPattern.matcher(sourceCode);

        while (matcher.find()) {
            hash = matcher.group(1);
            query = matcher.group(2);
            modifiedCode = matcher.group(3);
            matchedStart = matcher.start();
            matchedEnd = matcher.end();
            if (start >= matchedStart && start <= matchedEnd) {
                int finalMatchedStart = matchedStart;
                int finalMatchedEnd = matchedEnd;
                String finalModifiedCode = modifiedCode.trim();
                final Runnable runnable = () -> document.replaceString(finalMatchedStart, finalMatchedEnd, finalModifiedCode);
                WriteCommandAction.runWriteCommandAction(project, runnable);
                if (!UploadHttpClient.sendEditData(finalModifiedCode, config.getUserName(), project.getName(),
                        file.getName(), document.getText(), query, hash)) {
                    UndoManager.getInstance(project).undo(FileEditorManager.getInstance(project).getSelectedEditor());
                    HintManager.getInstance().showErrorHint(editor, "Error: Upload failed.");
                }
                return;
            }
        }
        HintManager.getInstance().showErrorHint(editor, "Error: Cursor position not inside generated block.");
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

