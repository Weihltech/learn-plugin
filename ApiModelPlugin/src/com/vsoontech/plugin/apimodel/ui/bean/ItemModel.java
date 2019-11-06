package com.vsoontech.plugin.apimodel.ui.bean;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;

public class ItemModel {

    public PsiClass mPsiClass;
    public String name;
    public boolean isSel;
    public String desc;
    public int lightLength;

    public ItemModel(PsiClass mPsiClass, String name) {
        this.mPsiClass = mPsiClass;
        this.name = name;
        this.isSel = false;

        String psiContent = mPsiClass.getContainingFile().getText();
        if (psiContent.lastIndexOf(":") > 0) {
            desc = psiContent.substring(psiContent.lastIndexOf(":"), psiContent.length());
        }
    }

    @Override
    public String toString() {
        return "ItemModel{" +
                "name='" + name + '\'' +
                ", isSel=" + isSel +
                '}';
    }
}
