package com.vsoontech.plugin;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.util.PsiUtilBase;
import com.vsoontech.plugin.apimodel.AnActionHelper;
import com.vsoontech.plugin.apimodel.Logc;
import com.vsoontech.plugin.apimodel.PsiHelper;
import com.vsoontech.plugin.apimodel.ui.AssignRespsDialog;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class AmGenerateAction extends BaseGenerateAction {


    public AmGenerateAction() {
        super(null);
    }

    protected AmGenerateAction(CodeInsightActionHandler handler) {
        super(handler);
    }

    private void generateModel() {
        Project project = AnActionHelper.getProject();
        Editor editor = AnActionHelper.getEditor();
        PsiFile mFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        assert mFile != null;
        PsiClass modelPsiClass = getTargetClass(editor, mFile);
        Logc.d("target model : " + modelPsiClass.getName());

        // Resp File Dir
        PsiDirectory respPsiDirectory = PsiHelper.findGenOutSrcPsiDirectory(
                mFile.getContainingDirectory(), project.getName(), AnActionHelper.getApiProperties().getApiOutSrc());

        if (respPsiDirectory != null) {
            AssignRespsDialog assignRespsDialog = new AssignRespsDialog(modelPsiClass, respPsiDirectory);
            assignRespsDialog.setSize(650, 500);
            assignRespsDialog.setLocationRelativeTo(null);
            assignRespsDialog.setVisible(true);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        AnActionHelper.init(anActionEvent);
        Logc.outputLogFile(AnActionHelper.getProject().getBasePath());
        generateModel();
    }
}
