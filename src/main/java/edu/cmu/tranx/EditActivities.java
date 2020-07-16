package edu.cmu.tranx;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditActivities implements FileEditorManagerListener {
    private final Project project;
    private DocumentEvent curEvent;
    private final TranXConfig config = TranXConfig.getInstance();

    public EditActivities(Project project) {
        this.project = project;
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager manager, @NotNull VirtualFile file) {
        FileEditor fileEditor = manager.getSelectedEditor(file);
        Document document = FileDocumentManager.getInstance().getDocument(file);

        // documents are available for text files only, we do not support image editors, for example
        if (fileEditor == null || document == null) {
            return;
        }

        // upload fine-grained edits only on .py files
        String ext = file.getExtension();
        if (ext == null || !ext.equals("py")) {
            return;
        }

        DeferredDocumentListener listener = new DeferredDocumentListener(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Execute your required functionality here...
                uploadFineGrainedEdit(curEvent.getDocument());
            }
        }, false);

        document.addDocumentListener(listener, fileEditor);
    }

    private void uploadFineGrainedEdit(Document document) {
        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        // ensure userId set in settings
        if (config == null || config.getUserName() == null || config.getUserName().equals("")) {
            System.err.println("Error: UserID not set in plugin settings");
            return;
        }

        if (file == null) {
            System.err.println("Error: file null");
            return;
        }

        if (!UploadHttpClient.sendFineGrainedEditData(config.getUserName(), project.getName(), file.getName(),
                document.getText())) {
            System.err.println("Fine grained edit upload failed!");
        }
    }

    public class DeferredDocumentListener implements DocumentListener {

        private final Timer timer;

        public DeferredDocumentListener(int timeOut, ActionListener listener, boolean repeats) {
            timer = new Timer(timeOut, listener);
            timer.setRepeats(repeats);
        }

        public void start() {
            timer.start();
        }

        public void stop() {
            timer.stop();
        }

        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            curEvent = event;
            timer.restart();
        }

    }


}
