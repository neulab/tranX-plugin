package edu.cmu.tranx;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class GetEdit extends AnAction {
    private static final String GEN_PATTERN =
            "(?s)# ---- BEGIN AUTO-GENERATED CODE ----\n\\s*# to remove these comments and send feedback press alt-G\\s(.*)\n\\s*# ---- END AUTO-GENERATED CODE ----";

    private Pattern genPattern = Pattern.compile(GEN_PATTERN);


    @Override
    public void actionPerformed(final AnActionEvent anActionEvent) {
        //Get all the required data from data keys
        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);
        final Document document = editor.getDocument();
        String sourceCode = document.getText();

        int matchedStart, matchedEnd;
        String modifiedCode;
        Matcher matcher = genPattern.matcher(sourceCode);
        if (matcher.find()) {
            modifiedCode = matcher.group(1);
            matchedStart = matcher.start();
            matchedEnd = matcher.end();
        }
        else {
            return;
        }

        System.out.println(modifiedCode);
        System.out.println(matchedStart);
        System.out.println(matchedEnd);

        int finalMatchedStart = matchedStart;
        int finalMatchedEnd = matchedEnd;
        String finalModifiedCode = modifiedCode.trim();
        final Runnable runnable = () -> document.replaceString(finalMatchedStart, finalMatchedEnd, finalModifiedCode);
        WriteCommandAction.runWriteCommandAction(project, runnable);

        // TODO: add actual upload code
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

