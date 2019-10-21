package com.vsoontech.plugin.apimodel;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.PsiJavaFileImpl;

import static com.intellij.openapi.util.text.StringUtil.isEmpty;

public class PsiHelper {
    public static PsiDirectory findGenOutSrcPsiDirectory(PsiDirectory psiDirectory, String projectName, String pkg) {
//        Logc.d("--- projectName = " + projectName + " ; psiFile = " + psiFile + " ; pkg = " + pkg);
        if (psiDirectory != null
                && !isEmpty(projectName) && !isEmpty(pkg)) {
//            Logc.d("--- projectName = " + projectName);
            while (psiDirectory != null
                    && !projectName.equals(psiDirectory.getName())) {
                psiDirectory = psiDirectory.getParent();
            }

            if (psiDirectory != null) {
//                Logc.d("--- Project : " + psiDirectory.getName());
                String genOutSrcPath = AnActionHelper.getApiProperties().getGenOutSrcPath();
//                Logc.d("--- genOutSrcPath : " + genOutSrcPath);
                String[] srcArgs = genOutSrcPath.split("\\.");
//                Logc.d("--- srcArgs : " + Arrays.toString(srcArgs));
                for (String srcArg : srcArgs) {
                    psiDirectory = psiDirectory.findSubdirectory(srcArg);
                    if (psiDirectory == null) {
                        break;
                    }
//                    Logc.d("---> " + psiDirectory.getName());
                }
                return psiDirectory;
            }
        }
        return null;
    }

    public static PsiDirectory getJavaSrc(PsiFile psiFile) {
        PsiDirectory psiDirectory = null;
        if (psiFile instanceof PsiJavaFileImpl) {
            String packageName = ((PsiJavaFileImpl) psiFile).getPackageName();
            String[] arg = packageName.split("\\.");
            Logc.d("PsiHelper : packageName = " + packageName);
            psiDirectory = psiFile.getContainingDirectory();

            for (int i = 0; i < arg.length; i++) {
                psiDirectory = psiDirectory.getParent();
                if ("src".equals(psiDirectory.getName())) {
                    break;
                }
            }
        }
        return psiDirectory;
    }

    public static PsiDirectory findGeneratePsiDirectory(PsiFile psiFile, String apiOutSrc) {
        PsiDirectory psiDirectory = psiFile.getContainingDirectory();
        String[] genSrc = {"build", "generated", "source"
                , "buildConfig", "release", "com", "vsoontech", "plugin", "api"};
        if (psiFile instanceof PsiJavaFileImpl) {

            do {
                Logc.d("findGeneratePsiDirectory : " + psiDirectory.getName());
                if ("ApiModelTest".equals(psiDirectory.getName())) {
                    for (String arg : genSrc) {
                        psiDirectory = psiDirectory.findSubdirectory(arg);
                        if (psiDirectory != null) {
                            Logc.d("findGeneratePsiDirectory : " + psiDirectory.getName());
                        }

                    }
                } else {
                    psiDirectory = psiDirectory.getParent();
                }
            } while (psiDirectory != null);

        }


        return null;
    }
}
