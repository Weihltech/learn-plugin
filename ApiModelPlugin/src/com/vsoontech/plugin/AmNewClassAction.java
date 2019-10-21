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

public class AmNewClassAction extends AnAction {


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


    // 提供创建 Model的入口
    private void createModelDialog() {
        Project project = AnActionHelper.getProject();
        PsiDirectory psiDirectory = AnActionHelper.getPsiDirectory();
        PsiDirectory pkgPsiDirectory = PsiHelper.findGenOutSrcPsiDirectory(
                psiDirectory, project.getName(), AnActionHelper.getApiProperties().getApiOutSrc());

        if (pkgPsiDirectory != null) {
            ArrayList<PsiClass> respPsiClassList = collectRespPsiClass(pkgPsiDirectory);
            if (!respPsiClassList.isEmpty()) {
                CreateModelDialog modelDialog = new CreateModelDialog(respPsiClassList);
                modelDialog.setSize(650, 500);
                modelDialog.setLocationRelativeTo(null);
                modelDialog.setVisible(true);
            } else {
                Logc.d("Not Find Resp.Java Files !");
            }
        }


    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        AnActionHelper.init(anActionEvent);
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
