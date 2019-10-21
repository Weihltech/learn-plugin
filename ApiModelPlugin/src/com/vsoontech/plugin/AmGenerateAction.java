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

        PsiDirectory pkgPsiDirectory = PsiHelper.findGenOutSrcPsiDirectory(
                mFile.getContainingDirectory(), project.getName(), AnActionHelper.getApiProperties().getApiOutSrc());

        if (pkgPsiDirectory != null) {
            ArrayList<PsiClass> respPsiClassList = collectRespPsiClass(pkgPsiDirectory);
            AssignRespsDialog assignRespsDialog = new AssignRespsDialog(modelPsiClass, respPsiClassList);
            assignRespsDialog.setSize(650, 500);
            assignRespsDialog.setLocationRelativeTo(null);
            assignRespsDialog.setVisible(true);
        }
    }


    private ArrayList<PsiClass> collectRespPsiClass(PsiDirectory pkgPsiDirectory) {
        if (pkgPsiDirectory != null) {
            Logc.d(pkgPsiDirectory.getName());
            ArrayList<PsiClass> psiClassList = new ArrayList<>();
            for (PsiFile psiFile : pkgPsiDirectory.getFiles()) {
                if (psiFile.getName().endsWith("Resp.java") && psiFile instanceof PsiJavaFile) {
                    PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                    psiClassList.add(psiJavaFile.getClasses()[0]);
                }
            }
            return psiClassList;
        }
        return null;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        AnActionHelper.init(anActionEvent);
        generateModel();
    }
}
