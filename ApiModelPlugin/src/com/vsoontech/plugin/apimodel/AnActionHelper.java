package com.vsoontech.plugin.apimodel;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;

public class AnActionHelper {

    private static AnActionEvent anActionEvent;
    private static ApiProperties mApiProperties;

    public static void init(AnActionEvent e) {
        anActionEvent = e;
        mApiProperties = new ApiProperties().read();
    }

    public static Project getProject() {
        return anActionEvent.getProject();
    }

    public static VirtualFile getVirtualFile() {
        return anActionEvent.getData(CommonDataKeys.VIRTUAL_FILE);
    }

    public static PsiDirectory getPsiDirectory() {
        Project mProject = getProject();
        VirtualFile mVirtualFile = getVirtualFile();
        return PsiManager.getInstance(mProject).findDirectory(mVirtualFile);
    }

    public static Editor getEditor() {
        return anActionEvent.getData(PlatformDataKeys.EDITOR);
    }

    public static ApiProperties getApiProperties() {
        return mApiProperties;
    }
}
