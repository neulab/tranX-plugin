package edu.cmu.tranx;



import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;


/**
 * Created by williamqian on 5/29/17.
 */
public class TextBoxes extends AnAction {
    public TextBoxes(){
        super("Text _Boxes");
    }
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = getEventProject(e);
        String txt= Messages.showInputDialog(project,"What is your Question?", "Query", Messages.getQuestionIcon());
        Messages.showMessageDialog(project, "You searched for this: " + txt + "\n Here are some results!", "Information", Messages.getQuestionIcon());
    }
}
