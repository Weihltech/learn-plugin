package com.vsoontech.plugin;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
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
import com.vsoontech.plugin.apimodel.ui.CreateModelDialog;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class AmNewClassAction extends AnAction {

    // 提供创建 Model的入口
    private void createModelDialog() {
        Project project = AnActionHelper.getProject();
        PsiDirectory psiDirectory = AnActionHelper.getPsiDirectory();
        // Resp File Dir
        PsiDirectory respPsiDirectory = PsiHelper.findGenOutSrcPsiDirectory(
                psiDirectory, project.getName(), AnActionHelper.getApiProperties().getApiOutSrc());

        if (respPsiDirectory != null) {
            CreateModelDialog modelDialog = new CreateModelDialog(respPsiDirectory);
            modelDialog.setSize(650, 500);
            modelDialog.setLocationRelativeTo(null);
            modelDialog.setVisible(true);
        }


    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        AnActionHelper.init(anActionEvent);
        Logc.outputLogFile(AnActionHelper.getProject().getBasePath());
        createModelDialog();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
//        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
//        String str = virtualFile.getPath().replace("/", ".");
        getTemplatePresentation().setIcon(AllIcons.Actions.GroupByClass);
//        getTemplatePresentation().setVisible(str.contains("src.main.java") && virtualFile.isDirectory());
    }
}
