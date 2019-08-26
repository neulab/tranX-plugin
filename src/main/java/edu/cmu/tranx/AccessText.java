package edu.cmu.tranx;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;

import java.util.ArrayList;

/**
 *
 */
public class AccessText extends AnAction {
    public static String[] resend=new String[3];
    public String recievedQuestion(final String txt,final Project project){
        String ans="";

        Messages.showMessageDialog(project, "You searched for this: " + txt + "\n Here are some results!", "Information",
                Messages.getInformationIcon());

        return "Hello World!";
    }

    public String replaceSTR(String answer, String query){
        String substr=null;
        String ret=null;
        if(answer.lastIndexOf("STR:")!=-1){

            if(query.indexOf("\"")!=-1){
                substr=query.substring(query.indexOf("\"")+1,query.lastIndexOf("\""));

            }
            if(query.indexOf("\'")!=-1){
                substr=query.substring(query.indexOf("\'")+1,query.lastIndexOf("\'"));

            }
        }
        if(substr!=null){
            ret=answer.replaceAll("_STR:(\\d+)_",substr);

        }
        else{
            ret=answer;

        }
        return ret;

    }

    @Override
    public void actionPerformed(final AnActionEvent anActionEvent) {
        //Get all the required data from data keys
        final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = anActionEvent.getRequiredData(CommonDataKeys.PROJECT);
        //Access document, caret, and selection
        final Document document = editor.getDocument();
        final SelectionModel selectionModel = editor.getSelectionModel();

        final int start = selectionModel.getSelectionStart();
        final int end = selectionModel.getSelectionEnd();
        //Replace the input box with its own class so that it does not interfere with the actual extraciton
        // of the queries

        final String query= Messages.showInputDialog(project,"What is your Question?","Query",
                Messages.getQuestionIcon());
        resend[0]=query;
        //System.out.print(Checkin.uname);
        try {
            ArrayList<HttpClient.Hypothesis> options = HttpClient.sendData(query).hypotheses;
            System.out.print(query);
            BaseListPopupStep<HttpClient.Hypothesis> q_list = new BaseListPopupStep<HttpClient.Hypothesis>
                    ("You searched" + " for: " + query + " here is a list of results", options) {
                @Override
                public String getTextFor(HttpClient.Hypothesis value) {
                    // String q = replaceSTR(value.query, query);
                    return "id: " + Integer.toString(value.id) + "\t" + "score: " + Double.toString(value.score) + "\n" +
                            "snippet: " + value.value;
                }

                @Override
                public PopupStep onChosen(HttpClient.Hypothesis selectedValue, boolean finalChoice) {
                    // String ans = selectedValue.query;
                    // final String answer = replaceSTR(ans, query);
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            document.replaceString(start, end, "# ---- BEGIN AUTO-GENERATED CODE ----\n" +
                                    "# to remove these comments and send feedback press alt-G\n" + selectedValue.value +
                                    "\n# ---- END AUTO-GENERATED CODE ----\n");
                            resend[1] = selectedValue.value;
                        }
                    };
                    WriteCommandAction.runWriteCommandAction(project, runnable);
                    return super.onChosen(selectedValue, finalChoice);
                }


            };

            /*final String ans= recievedQuestion(txt, project);
            //New instance of Runnable to make a replacement
            if (txt!= null) {


                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                        document.replaceString(start, end, txt);

                    }
                };
                final Runnable runnable2 = new Runnable() {
                    @Override
                    public void run() {
                        document.replaceString(start, end, txt);
                    }
                };
                Runnable runnablePrint1 = new Runnable() {
                    @Override
                    public void run() {
                        WriteCommandAction.runWriteCommandAction(project, runnable);
                       // selectionModel.removeSelection();
                    }
                };
                Runnable runnablePrint2 = new Runnable() {
                    @Override
                    public void run() {
                        WriteCommandAction.runWriteCommandAction(project, runnable2);
                       // selectionModel.removeSelection();
                    }
                };*/
            //Making the replacement
            JBPopupFactory jbPopupFactory = JBPopupFactory.getInstance();

            //dead code

               /* try {
                    //ProcessBuilder pb= new ProcessBuilder("cd ~/NL2code/ && . ~/NL2code/run_interactive.sh django");
                    Process p=Runtime.getRuntime().exec("cd ~/NL2code/ && . ~/NL2code/run_interactive.sh django");

                    try {
                        p.waitFor();
                    } catch(InterruptedException e1){
                        System.out.print("Terminated unexpectedly");
                    }
                    //p.waitFor();

                    //Runtime.getRuntime().exec("cd ~/NL2code/ && . ~/NL2code/run_interactive.sh django");
                    //Runtime.getRuntime().exec(". run_interactive.sh django");
                    System.out.print("Success!\n");
                }
                catch (IOException e2){
                    System.out.print("Cant find script\n");
                }*/
            jbPopupFactory.createListPopup(q_list).show(jbPopupFactory.guessBestPopupLocation(editor));

            selectionModel.removeSelection();
            //extracting the code history

            //System.out.print(History.update(anActionEvent));

            //JBPopup.show(jbPopupFactory.guessBestPopupLocation(editor));
            //WriteCommandAction.runWriteCommandAction(project, runnable);
            //selectionModel.removeSelection();
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

