package edu.cmu.tranx;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class EditorStatusComponent extends AbstractProjectComponent {
    protected EditorStatusComponent(Project project) {
        super(project);
    }

    @Override
    public void projectOpened() {
        MessageBusConnection connection = myProject.getMessageBus().connect(myProject);
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager manager, @NotNull VirtualFile file) {
                FileEditor fileEditor = manager.getSelectedEditor(file);
                Document document = FileDocumentManager.getInstance().getDocument(file);

                // documents are available for text files only, we do not support image editors, for example
                if (fileEditor == null || document == null) {
                    return;
                }

                JBLabel label = new JBLabel();
                label.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
                label.setText(statusMessage(document, file));

                manager.addTopComponent(fileEditor, label);

                document.addDocumentListener(new DocumentListener() {
                    @Override
                    public void documentChanged(@NotNull DocumentEvent event) {
                        label.setText(statusMessage(event.getDocument(),
                                FileDocumentManager.getInstance().getFile(event.getDocument())));
                    }
                }, fileEditor);
            }
        });
    }

    private String statusMessage(Document doc, VirtualFile file) {
        System.out.println(doc.getCharsSequence().toString());
        System.out.println(file.getName());

        NumberFormat format = DecimalFormat.getIntegerInstance();

        return StringUtil.formatFileSize(file.getLength()) + ", " +
                file.getCharset().displayName() + " " +
                format.format(doc.getTextLength()) + " chars, " +
                format.format(doc.getLineCount()) + " lines";
    }

}
