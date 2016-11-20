package de.neuland.hybris.language.groovy.action;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;


public class GenerateBeanAction extends AnAction
{

    @Override
    public void actionPerformed(AnActionEvent e)
    {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        int offset = editor.getCaretModel().getOffset() - 1;
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        PsiElement current = psiFile.findElementAt(offset);

        String classStr = current.getText();

        StringBuilder builder = new StringBuilder(classStr);
        String field = classStr.substring(0, 1).toLowerCase() + classStr.substring(1);
        builder.append(" ").append(field).append(" = ").append(field).append("\n");

        Document document = editor.getDocument();
        WriteCommandAction.runWriteCommandAction(editor.getProject(), () ->
        {
            ReadonlyStatusHandler.getInstance(editor.getProject()).ensureFilesWritable();

            int elOffset = current.getTextOffset();
            document.replaceString(elOffset, elOffset + classStr.length(), field);

            String text = builder.append(document.getText()).toString();
            document.setText(text);
        });
    }

    @Override
    public void update(AnActionEvent e)
    {
        Language language = e.getData(LangDataKeys.PSI_FILE).getLanguage();
        boolean isGroovy = language != null && language.getID().equalsIgnoreCase("Groovy");
        e.getPresentation().setEnabledAndVisible(isGroovy);
    }

}
