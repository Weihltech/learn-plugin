package com.vsoontech.plugin.apimodel;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.vsoontech.plugin.AmGenerateAction;

import java.util.ArrayList;
import java.util.HashMap;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ApiModelGenerate {

    private Project project;
    private PsiClass modelPsiClass;
    private PsiDirectory psiDirectory;
    private PsiElementFactory psiElementFactory;
    private PsiJavaFile modelPsiFile;
    private CallBack mCallBack;
    private HashMap<String, PsiField> modelPsiClassNewFields;
    private HashMap<String, PsiClass> modelPsiClassNewInnerClasses;
    private ArrayList<PsiClass> selRespClassList;

    public interface CallBack {
        public void finish();
    }

    public ApiModelGenerate(PsiClass targetPsiClass, CallBack callBack) {
        if (targetPsiClass != null) {
            mCallBack = callBack;
            modelPsiClass = targetPsiClass;
            modelPsiFile = (PsiJavaFile) modelPsiClass.getContainingFile();
            psiDirectory = AnActionHelper.getPsiDirectory();
            project = AnActionHelper.getProject();
            psiElementFactory = JavaPsiFacade.getElementFactory(project);
            modelPsiClassNewFields = new HashMap<>();
            modelPsiClassNewInnerClasses = new HashMap<>();
        }
    }

    public void generate(ArrayList<PsiClass> selRespPsiClassList) {
        if (selRespPsiClassList != null
                && !selRespPsiClassList.isEmpty()) {
            selRespClassList = selRespPsiClassList;
            WriteCommandAction.runWriteCommandAction(project, () -> {
                for (PsiClass respClass : selRespPsiClassList) {
                    // fields
                    collectDiffFields(respClass);
                    collectAndMergeInnerClass(respClass);
                    // constructor
                }

                updateImport();
                updateModelFields();
                updateModelInnerClass();
                updateModelConstructors();
                addOnConstructor(modelPsiClass);

                // 格式化和去掉无用引用
                JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(project);
                styleManager.shortenClassReferences(modelPsiClass);
                styleManager.optimizeImports(modelPsiFile);
                styleManager.removeRedundantImports(modelPsiFile);
                if (mCallBack != null) {
                    mCallBack.finish();
                }
            });

        }
    }

    private void addOnConstructor(PsiClass psiClass) {
        PsiMethod[] onConstructorMethods = psiClass.findMethodsByName("onConstructor", false);
        if (onConstructorMethods.length <= 0) {
            PsiMethod onConstructorMethod = psiElementFactory.createMethodFromText("void onConstructor(){// do Something\n}", modelPsiClass);
            psiClass.add(onConstructorMethod);
        }
    }

    private void updateModelConstructors() {
        for (PsiClass respPsiClass : selRespClassList) {
            PsiMethod cPsiMethod = findConstructor(modelPsiClass, respPsiClass);
            if (cPsiMethod != null) {
                // 删除重新创建
                cPsiMethod.delete();
            }
            cPsiMethod = createConstructorMethod(modelPsiClass, respPsiClass);
            modelPsiClass.add(cPsiMethod);
        }
    }

    private PsiMethod createConstructorMethod(PsiClass origiClass, PsiClass paramClass) {
        StringBuilder contentBuilder = new StringBuilder();
        String paramClassLowerName = paramClass.getName().toLowerCase();
        contentBuilder.append("public ").append(origiClass.getName()).append("(")
                .append(paramClass.getName()).append(" ").append(paramClassLowerName)
                .append("){");
        for (PsiField paramField : paramClass.getAllFields()) {
            if (isSampleField(paramField)) {
                createSampleFieldContent(contentBuilder, paramField, paramClassLowerName);
            } else if (isListField(paramField)) {
                // 列表
                if (!createSampleListFieldContent(contentBuilder, paramField, paramClassLowerName)) {
                    createAssignObjListFieldContent(contentBuilder, paramField, paramClassLowerName, paramClass);
                }
            } else {
                //this.autor = new Autor(courseresp.autor);
                // 先产生构造函数
                createAssignObjFieldContent(contentBuilder, paramField, paramClassLowerName, paramClass);
            }
        }
        contentBuilder.append("\n").append("onConstructor();");
        contentBuilder.append("}");
        return psiElementFactory.createMethodFromText(contentBuilder.toString(), modelPsiClass);
    }

    private void createAssignObjListFieldContent(
            StringBuilder contentBuilder, PsiField paramField, String paramClassLowerName, PsiClass paramClass) {
        //         if (livemaybelistresp.purchasedList != null && !livemaybelistresp.purchasedList.isEmpty()) {
        //            this.purchasedList = new ArrayList<>();
        //            for (LiveMaybeListResp.Purchased purchased : livemaybelistresp.purchasedList) {
        //                purchasedList.add(new Purchased(purchased));
        //            }
        //        }
        String subTargetClsName = paramField.getType().getCanonicalText();
        subTargetClsName = subTargetClsName.substring(
                subTargetClsName.lastIndexOf("<") + 1, subTargetClsName.length() - 1);
        if (subTargetClsName.contains(".")) {
            subTargetClsName = subTargetClsName.substring(subTargetClsName.lastIndexOf(".") + 1, subTargetClsName.length());
        }
        Logc.d("createAssignObjListFieldContent ---" + subTargetClsName);
        if (createInnerConstructorMethod(paramClass, subTargetClsName)) {
            contentBuilder.append("if(").append(paramClassLowerName).append(".").append(paramField.getName()).append("!= null && !")
                    .append(paramClassLowerName).append(".").append(paramField.getName()).append(".isEmpty()) {\n")
                    .append("this.").append(paramField.getName()).append(" = new ArrayList<>();\n")
                    .append("for(").append(paramClass.getName()).append(".").append(subTargetClsName).append(" ").append(subTargetClsName.toLowerCase())
                    .append(" : ").append(paramClassLowerName).append(".").append(paramField.getName()).append("){\n")
                    .append("this.").append(paramField.getName()).append(".add(new ").append(subTargetClsName).append("(").append(subTargetClsName.toLowerCase())
                    .append("));\n}\n}\n");
        }
    }

    private boolean createInnerConstructorMethod(PsiClass paramClass, String paramClsName) {
        PsiClass modelTargetPsiClass = modelPsiClass.findInnerClassByName(paramClsName, false);
        PsiClass paramTargetPsiClass = paramClass.findInnerClassByName(paramClsName, false);
        if (paramTargetPsiClass != null && modelTargetPsiClass != null) {

            StringBuilder contentBuilder = new StringBuilder();
            String paramClassLowerName = paramClsName.toLowerCase();
            contentBuilder.append("public ").append(modelTargetPsiClass.getName()).append("(")
                    .append(paramClass.getName()).append(".").append(paramClsName).append(" ").append(paramClassLowerName)
                    .append("){");

            PsiMethod cPsiMethod = findInnerConstructor(modelTargetPsiClass, paramClass, paramTargetPsiClass, paramClsName);
            if (cPsiMethod != null) {
//                return true;
                cPsiMethod.delete();
            }

            for (PsiField paramField : paramTargetPsiClass.getFields()) {
                if (isSampleField(paramField)) {
                    createSampleFieldContent(contentBuilder, paramField, paramClassLowerName);
                } else if (isListField(paramField)) {
                    // 列表
                    if (!createSampleListFieldContent(contentBuilder, paramField, paramClassLowerName)) {
                        createAssignObjListFieldContent(contentBuilder, paramField, paramClassLowerName, paramClass);
                    }
                } else {
                    //this.autor = new Autor(courseresp.autor);
                    // 先产生构造函数
                    createAssignObjFieldContent(contentBuilder, paramField, paramClassLowerName, paramClass);
                }
            }

            String constructorContent = contentBuilder.append("\n").append("onConstructor();").append("}").toString();
            PsiMethod method = psiElementFactory.createMethodFromText(constructorContent, modelTargetPsiClass);
            modelTargetPsiClass.add(method);
        }

        return true;
    }

    private void createAssignObjFieldContent(StringBuilder contentBuilder, PsiField paramField, String paramClassLowerName, PsiClass paramClass) {
        String subTargetClsName = paramField.getType().getCanonicalText();
        subTargetClsName = subTargetClsName.substring(
                subTargetClsName.lastIndexOf(".") + 1, subTargetClsName.length());
        Logc.d("createAssignObjFieldContent ---" + subTargetClsName);
        if (createInnerConstructorMethod(paramClass, subTargetClsName)) {
            contentBuilder.append("this.").append(paramField.getName()).append(" = ")
                    .append("new ").append(subTargetClsName).append("(")
                    .append(paramClassLowerName).append(".").append(paramField.getName()).append(");");
        }
    }

    private boolean createSampleListFieldContent(StringBuilder contentBuilder, PsiField paramField, String paramClassLowerName) {
        //        this.array = new ArrayList<>();
        //        this.array.addAll(livemaybelistresp.array);
        String targetClsName = paramField.getType().getCanonicalText();
//        Logc.d("---->>> " + targetClsName);
        if (targetClsName.contains("<java.lang.String>")
                || targetClsName.contains("<java.lang.Integer>")
                || targetClsName.contains("<java.lang.Boolean>")) {
            contentBuilder.append("this.").append(paramField.getName()).append(" = new ArrayList<>();\n")
                    .append("this.").append(paramField.getName()).append(".addAll(")
                    .append(paramClassLowerName).append(".").append(paramField.getName()).append(");");
            return true;
        }
        return false;
    }

    private void createSampleFieldContent(
            StringBuilder contentBuilder, PsiField paramField, String paramClassLowerName) {
        // : this.autor = resp.autor;
        contentBuilder.append("this.").append(paramField.getName()).append(" = ")
                .append(paramClassLowerName).append(".").append(paramField.getName()).append(";");
    }

    private boolean isListField(PsiField psiField) {
        String typeStr = psiField.getType().toString();
        return typeStr.contains("List")
                || typeStr.contains("ArrayList");
    }

    private boolean isSampleField(PsiField psiField) {
        String typeStr = psiField.getType().toString();
        return "PsiType:String".equals(typeStr)
                || "PsiType:int".equals(typeStr)
                || "PsiType:boolean".equals(typeStr);
    }

    private PsiMethod findConstructor(PsiClass origiClass, PsiClass paramClass) {
        if (paramClass != null && origiClass != null && !isEmpty(paramClass.getName())) {
            String parameterStr = "PsiParameterList:(" + paramClass.getName() + " " + paramClass.getName().toLowerCase() + ")";
            PsiMethod[] modelConstructors = origiClass.getConstructors();
            for (PsiMethod cPsiMethod : modelConstructors) {
                String cParameterStr = cPsiMethod.getParameterList().toString();
//                Logc.d("findInnerConstructor >>>> parameterStr = " + parameterStr + " ; cParameterStr = " + cParameterStr);
                if (parameterStr.equals(cParameterStr)) {
                    // PsiParameterList:(Purchased purchased)
                    return cPsiMethod;
                }
            }
        }
        return null;
    }

    private PsiMethod findInnerConstructor(PsiClass origiClass, PsiClass paramClass, PsiClass paramTargetPsiClass, String paramClsName) {
        String parameterStr = "PsiParameterList:(" + paramClass.getName() + "." + paramTargetPsiClass.getName() + " " + paramClsName.toLowerCase() + ")";
        PsiMethod[] modelConstructors = origiClass.getConstructors();
        for (PsiMethod cPsiMethod : modelConstructors) {
            String cParameterStr = cPsiMethod.getParameterList().toString();
//            Logc.d("findInnerConstructor >>>> parameterStr = " + parameterStr + " ; cParameterStr = " + cParameterStr);
            if (parameterStr.equals(cParameterStr)) {
                // PsiParameterList:(MutilInnerListResp.Purchased purchased)
                return cPsiMethod;
            }
        }
        return null;
    }

    private void updateModelInnerClass() {
        for (PsiClass nModelInnerClass : modelPsiClassNewInnerClasses.values()) {
            addOnConstructor(nModelInnerClass);
            modelPsiClass.add(nModelInnerClass);
        }
    }

    private void collectAndMergeInnerClass(PsiClass respClass) {
        PsiClass[] innerClasses = respClass.getInnerClasses();
        for (PsiClass respInnerClass : innerClasses) {
            if (!isEmpty(respInnerClass.getName())) {
                if (!modelPsiClassNewInnerClasses.containsKey(respInnerClass.getName())
                        && modelPsiClass.findInnerClassByName(respInnerClass.getName(), false) == null) {
                    PsiClass modelInnerNewClass = psiElementFactory.createClass(respInnerClass.getName());
                    for (PsiField respInnerClassField : respInnerClass.getAllFields()) {
                        PsiField newField = psiElementFactory.createFieldFromText(respInnerClassField.getText(), modelInnerNewClass);
                        modelInnerNewClass.add(newField);
                    }
                    modelPsiClassNewInnerClasses.put(modelInnerNewClass.getName(), modelInnerNewClass);
                } else {
                    // 更新字段
                    PsiClass modelInnerNewClass = modelPsiClassNewInnerClasses.get(respInnerClass.getName());
                    if (modelInnerNewClass == null) {
                        modelInnerNewClass = modelPsiClass.findInnerClassByName(respInnerClass.getName(), false);
                    }
                    if (modelInnerNewClass != null) {
                        for (PsiField respInnerClassField : respInnerClass.getAllFields()) {
                            if (modelInnerNewClass.findFieldByName(respInnerClassField.getName(), false) == null) {
                                PsiField newField = psiElementFactory.createFieldFromText(respInnerClassField.getText(), modelInnerNewClass);
                                modelInnerNewClass.add(newField);
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateModelFields() {
        // 添加不同字段
        for (PsiField nPsiField : modelPsiClassNewFields.values()) {
            modelPsiClass.add(nPsiField);
        }
    }

    private void collectDiffFields(PsiClass respClass) {
        // 收集不同的字段
        for (PsiField psiField : respClass.getAllFields()) {
            if (!modelPsiClassNewFields.containsKey(psiField.getName())
                    && modelPsiClass.findFieldByName(psiField.getName(), false) == null) {
                PsiField modelFiled = psiElementFactory.createFieldFromText(psiField.getText(), modelPsiClass);
                modelPsiClassNewFields.put(modelFiled.getName(), modelFiled);
            }
        }
    }

    private void updateImport() {
        // import  按 Resp.java 生成规则，包名引用最多增加 ArrayList;
        PsiImportList modelImportList = modelPsiFile.getImportList();
        if (modelImportList != null) {
            for (PsiClass psiClass : selRespClassList) {
                // Such as import java.util.*;
                PsiImportStatement statement = psiElementFactory.createImportStatement(psiClass);
                modelImportList.add(statement);
            }
            modelImportList.add(psiElementFactory.createImportStatementOnDemand("java.util"));
        }
    }


}
