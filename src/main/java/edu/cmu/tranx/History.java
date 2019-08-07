package edu.cmu.tranx;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

/**
 * Created by williamqian on 6/7/17.
 */
public class History {
    //@Override
    public static String update(final AnActionEvent e){
      return actionPerformed(e);
    }
    //@Override
    public static String actionPerformed(AnActionEvent e) {
        System.out.print("hello world");
        // TODO: insert action logic here
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        //final Project project=e.getRequiredData(CommonDataKeys.PROJECT);
        final Document document=editor.getDocument();
        String text=document.getText();
        return text;
       // System.out.print(text);

    }
}
