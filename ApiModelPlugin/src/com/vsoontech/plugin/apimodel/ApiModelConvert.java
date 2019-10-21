package com.vsoontech.plugin.apimodel;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;

import java.util.ArrayList;
import java.util.Objects;

import static com.intellij.openapi.util.text.StringUtil.isEmpty;

public class ApiModelConvert {

    private PsiDirectory psiDirectory;
    private PsiElementFactory psiElementFactory;
    private Project project;
    private PsiClass modelPsiClass;
    private PsiJavaFile modelPsiFile;
    private CallBack mCallBack;
    private ArrayList<PsiField> selNewPsiFieldList;
    private ArrayList<PsiClass> selNewPsiInnerClassList;
    private ArrayList<PsiClass> selNewPsiInnerClassSameList;// 同名内部类
    private ArrayList<PsiClass> selRespPsiClassList;
    private ArrayList<PsiImportStatement> selNewPsiImportStatementList;

    public interface CallBack {
        public void finish();
    }

    public ApiModelConvert(PsiClass targetPsiClass, CallBack callBack) {
        if (targetPsiClass != null) {
            mCallBack = callBack;
            modelPsiClass = targetPsiClass;
            modelPsiFile = (PsiJavaFile) modelPsiClass.getContainingFile();
            psiDirectory = AnActionHelper.getPsiDirectory();
            project = AnActionHelper.getProject();
            psiElementFactory = JavaPsiFacade.getElementFactory(project);
        }
    }

    public void generate(ArrayList<PsiClass> selPsiClassList) {
        selRespPsiClassList = selPsiClassList;

        if (modelPsiClass != null
                && selRespPsiClassList != null
                && !selRespPsiClassList.isEmpty()) {
            // correctModelClassField ； modelPsiClass.getFields();
            // correctSourceE();
            selNewPsiFieldList = new ArrayList<>();
            selNewPsiInnerClassList = new ArrayList<>();
            selNewPsiInnerClassSameList = new ArrayList<>();
            selNewPsiImportStatementList = new ArrayList<>();
            // 先收集 target Fields; 做字段对比

            PsiField[] modelPsiFields = modelPsiClass.getFields();
            PsiClass[] modelPsiInnerClass = modelPsiClass.getInnerClasses();
            // 遍历所有选中 PsiClass
            for (PsiClass psiClass : selRespPsiClassList) {

                // Such as import java.util.*;
                selNewPsiImportStatementList.add(psiElementFactory.createImportStatement(psiClass));

                // 过滤相同字段，并收集所选 PsiClass 字段集合
                for (PsiField psiField : psiClass.getFields()) {
                    // 目标无字段且收集集合无相同字段则标记为更新字段
                    if (!containsModelPsiFields(modelPsiFields, psiField)
                            && !containsPsiField(selNewPsiFieldList, psiField)) {
                        selNewPsiFieldList.add(newObjField(psiField));
                    }
                    if (isListField(psiField)) {
                        // Such as import java.util.*;
                        selNewPsiImportStatementList.add(psiElementFactory.createImportStatementOnDemand("java.util"));

                    }
                }
                // 过滤已收集相同类名 InnerPsiClass
                for (PsiClass innerPsiClass : psiClass.getInnerClasses()) {
                    if (!containsModelPsiInnerClass(modelPsiInnerClass, innerPsiClass)
                            && !containsPsiClass(selNewPsiInnerClassList, innerPsiClass)) {
                        selNewPsiInnerClassList.add(newObjClass(innerPsiClass));
                    } else {
                        // 独立收集后遍历相同类名 InnerPsiClass 做字段合并做准备
                        selNewPsiInnerClassSameList.add(newObjClass(innerPsiClass));
                    }
                }
            }

            // write out
            WriteCommandAction.runWriteCommandAction(project, new Runnable() {
                @Override
                public void run() {
                    // 更新 import 按生成规则就增加两种
                    updateImportList();

                    // 更新字段
                    for (PsiField psiField : selNewPsiFieldList) {
                        if (psiField != null) {
                            modelPsiClass.add(psiField.copy());
                        }
                    }
                    // 更新内部类
                    for (PsiClass innerPsiClass : selNewPsiInnerClassList) {
                        if (innerPsiClass != null) {
                            modelPsiClass.add(innerPsiClass);
                        }
                    }
                    // 合并已存在内部类，字段
                    mergeFieldsInSamePsiClass();
                    // 更新构造函数
                    createConstructorMethod();

                    CodeStyleManager.getInstance(project).reformat(modelPsiFile);
                    if (mCallBack != null) {
                        mCallBack.finish();
                    }
                }
            });
        }
    }

    private PsiClass newObjClass(PsiClass innerPsiClass) {
        if (innerPsiClass != null && !isEmpty(innerPsiClass.getName())) {
            PsiClass psiClass = psiElementFactory.createClass(innerPsiClass.getName());
            for (PsiField psiField : innerPsiClass.getFields()) {
                psiClass.add(newObjField(psiField));
            }
            return psiClass;
        }
        return null;
    }

    private void updateImportList() {
        PsiImportList psiImportList = modelPsiFile.getImportList();
        boolean existImport = false;
        if (psiImportList != null) {
            // 字段需要更新的 引用
            for (PsiImportStatement newImportStatement : selNewPsiImportStatementList) {
                // 是否已存在
                existImport = false;
                for (PsiImportStatement importStatement : psiImportList.getImportStatements()) {
                    if (Objects.equals(newImportStatement.getQualifiedName(), importStatement.getQualifiedName())) {
                        // 有相同，则忽视
                        existImport = true;
                        break;
                    }
                }
                if (!existImport) {
                    psiImportList.add(newImportStatement);
                }
            }
        }
    }

    private void createConstructorMethod() {
        for (PsiClass respPsiClass : selRespPsiClassList) {
            String constructorContent = createConstructorContent(respPsiClass);
            PsiMethod method = psiElementFactory.createMethodFromText(constructorContent, modelPsiClass);
            modelPsiClass.add(method);
        }
    }

    private String createConstructorContent(PsiClass respPsiClass) {
        StringBuilder builder = new StringBuilder();
        if (respPsiClass != null && !isEmpty(respPsiClass.getName())) {
            // start :  public CourseModel(Autor autor) {
            String respClassLowerName = respPsiClass.getName().toLowerCase();
            builder.append("public ").append(modelPsiClass.getName()).append("(")
                    .append(respPsiClass.getName()).append(" ").append(respClassLowerName)
                    .append("){");

            // fields
            for (PsiField psiField : respPsiClass.getFields()) {
                if (isSampleField(psiField)) {
                    // : this.autor = autor;
                    builder.append("this.").append(psiField.getName()).append(" = ")
                            .append(respClassLowerName).append(".").append(psiField.getName()).append(";");
                } else if (isListField(psiField)) {
                    // 集合包含的是普通类型，则直接赋值
                    String targetClsName = psiField.getType().getCanonicalText();
                    if (targetClsName.contains("<String>")
                            || targetClsName.contains("<Integer>")
                            || targetClsName.contains("<Boolean>")) {
                        //        this.array = new ArrayList<>();
                        //        this.array.addAll(livemaybelistresp.array);
                        builder.append("this.").append(psiField.getName()).append(" = new ArrayList<>();\n")
                                .append("this.").append(psiField.getName()).append(".addAll(")
                                .append(respClassLowerName).append(".").append(psiField.getName()).append(");");
                    } else {
                        targetClsName = targetClsName.substring(
                                targetClsName.lastIndexOf(".") + 1, targetClsName.length() - 1);
                        //         if (livemaybelistresp.purchasedList != null && !livemaybelistresp.purchasedList.isEmpty()) {
                        //            this.purchasedList = new ArrayList<>();
                        //            for (LiveMaybeListResp.Purchased purchased : livemaybelistresp.purchasedList) {
                        //                purchasedList.add(new Purchased(purchased));
                        //            }
                        //        }
                        if (createInnerConstructorContent(respPsiClass, targetClsName)) {
                            builder.append("if(").append(respClassLowerName).append(".").append(psiField.getName()).append("!= null && !")
                                    .append(respClassLowerName).append(".").append(psiField.getName()).append(".isEmpty()) {\n")
                                    .append("this.").append(psiField.getName()).append(" = new ArrayList<>();\n")
                                    .append("for(").append(respPsiClass.getName()).append(".").append(targetClsName).append(" ").append(targetClsName.toLowerCase())
                                    .append(" : ").append(respClassLowerName).append(".").append(psiField.getName()).append("){\n")
                                    .append("this.").append(psiField.getName()).append(".add(new ").append(targetClsName).append("(").append(targetClsName.toLowerCase())
                                    .append("));\n}\n}\n");
                        }
                    }

                } else {
                    //this.autor = new Autor(courseresp.autor);
                    // 先产生构造函数
                    String targetClsName = psiField.getType().getCanonicalText();
                    targetClsName = targetClsName.substring(
                            targetClsName.lastIndexOf(".") + 1, targetClsName.length());
                    if (createInnerConstructorContent(respPsiClass, targetClsName)) {
                        builder.append("this.").append(psiField.getName()).append(" = ")
                                .append("new ").append(targetClsName).append("(")
                                .append(respClassLowerName).append(".").append(psiField.getName()).append(");");
                    }
                }
            }

            // end :  }
            builder.append("}");
        }

        return builder.toString();
    }

    private boolean createInnerConstructorContent(PsiClass respPsiClass, String targetClsName) {
        PsiClass modelTargetPsiClass = modelPsiClass.findInnerClassByName(targetClsName, false);
        PsiClass respTargetPsiClass = respPsiClass.findInnerClassByName(targetClsName, false);
        if (respTargetPsiClass != null && modelTargetPsiClass != null) {

            StringBuilder builder = new StringBuilder();
            String respClassLowerName = targetClsName.toLowerCase();
            builder.append("public ").append(modelTargetPsiClass.getName()).append("(")
                    .append(respPsiClass.getName()).append(".").append(targetClsName).append(" ").append(respClassLowerName)
                    .append("){");
            // 判读是否存在相同构造函数，存在则删除，重新建立；
            PsiMethod[] modelConstructors = modelTargetPsiClass.getConstructors();
            for (PsiMethod cPsiMethod : modelConstructors) {
                String cParameterStr = cPsiMethod.getParameterList().toString();
                String parameterStr = "PsiParameterList:(" + respPsiClass.getName() + "." + targetClsName + " " + respClassLowerName + ")";
                Logc.d("--------- parameterStr = " + parameterStr + " ; >>> " + cParameterStr);
                if (parameterStr.equals(cParameterStr)) {
                    // PsiParameterList:(MutilInnerListResp.Purchased purchased)
                    return true;
                }
            }

            for (PsiField respTargetPsiField : respTargetPsiClass.getFields()) {
                if (isSampleField(respTargetPsiField)) {
                    // : this.autor = autor;
                    builder.append("this.").append(respTargetPsiField.getName()).append(" = ")
                            .append(respClassLowerName).append(".").append(respTargetPsiField.getName()).append(";");
                } else if (isListField(respTargetPsiField)) {

                    // 集合包含的是普通类型，则直接赋值
                    String subTargetClsName = respTargetPsiField.getType().getCanonicalText();
                    if (subTargetClsName.contains("<String>")
                            || subTargetClsName.contains("<Integer>")
                            || subTargetClsName.contains("<Boolean>")) {
                        //        this.array = new ArrayList<>();
                        //        this.array.addAll(livemaybelistresp.array);
                        builder.append("this.").append(respTargetPsiField.getName()).append(" = new ArrayList<>();\n")
                                .append("this.").append(respTargetPsiField.getName()).append(".addAll(")
                                .append(respClassLowerName).append(".").append(respTargetPsiField.getName()).append(");");
                    } else {
                        subTargetClsName = subTargetClsName.substring(
                                subTargetClsName.lastIndexOf(".") + 1, subTargetClsName.length() - 1);
                        //         if (livemaybelistresp.purchasedList != null && !livemaybelistresp.purchasedList.isEmpty()) {
                        //            this.purchasedList = new ArrayList<>();
                        //            for (LiveMaybeListResp.Purchased purchased : livemaybelistresp.purchasedList) {
                        //                purchasedList.add(new Purchased(purchased));
                        //            }
                        //        }
                        if (createInnerConstructorContent(respPsiClass, subTargetClsName)) {
                            builder.append("if(").append(respClassLowerName).append(".").append(respTargetPsiField.getName()).append("!= null && !")
                                    .append(respClassLowerName).append(".").append(respTargetPsiField.getName()).append(".isEmpty()) {\n")
                                    .append("this.").append(respTargetPsiField.getName()).append(" = new ArrayList<>();\n")
                                    .append("for(").append(respPsiClass.getName()).append(".").append(subTargetClsName).append(" ").append(subTargetClsName.toLowerCase())
                                    .append(" : ").append(respClassLowerName).append(".").append(respTargetPsiField.getName()).append("){\n")
                                    .append("this.").append(respTargetPsiField.getName()).append(".add(new ").append(subTargetClsName).append("(").append(subTargetClsName.toLowerCase())
                                    .append("));\n}\n}\n");
                        }
                    }
                } else {
                    //this.autor = new Autor(courseresp.autor);
                    // 先产生构造函数
                    String subTargetClsName = respTargetPsiField.getType().getCanonicalText();
                    subTargetClsName = subTargetClsName.substring(
                            subTargetClsName.lastIndexOf(".") + 1, subTargetClsName.length());
                    if (createInnerConstructorContent(respPsiClass, subTargetClsName)) {
                        builder.append("this.").append(respTargetPsiField.getName()).append(" = ")
                                .append("new ").append(subTargetClsName).append("(")
                                .append(respClassLowerName).append(".").append(respTargetPsiField.getName()).append(");");
                    }
                }
            }

            String constructorContent = builder.append("}").toString();
            PsiMethod method = psiElementFactory.createMethodFromText(constructorContent, modelTargetPsiClass);
            modelTargetPsiClass.add(method);

        }

        return true;
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

    private PsiField newObjField(PsiField psiField) {
        PsiField newField = psiElementFactory.createFieldFromText(psiField.getText(), modelPsiClass);
        return newField;
    }

    private boolean containsModelPsiInnerClass(PsiClass[] modelPsiInnerClass, PsiClass innerPsiClass) {
        for (PsiClass psiClass : modelPsiInnerClass) {
            // 这里不会有第二级，参照Resp实体类生成规则
            if (Objects.equals(psiClass.getName(), innerPsiClass.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsModelPsiFields(PsiField[] modelPsiFields, PsiField psiField) {
        for (PsiField tField : modelPsiFields) {
            if (isSameField(tField, psiField)) {
                return true;
            }
        }
        return false;
    }

    private void mergeFieldsInSamePsiClass() {
        for (PsiClass samePsiClass : selNewPsiInnerClassSameList) {
            PsiClass innerClass = modelPsiClass.findInnerClassByName(samePsiClass.getName(), true);
            boolean hasSameField = false;
            if (innerClass != null) {
                for (PsiField samePsiField : samePsiClass.getFields()) {
                    // 遍历 innerClass 是否存在相同字段
                    for (PsiField innerPsiField : innerClass.getFields()) {
                        if (isSameField(innerPsiField, samePsiField)) {
                            hasSameField = true;
                            break;
                        }
                    }
                    if (hasSameField) {
                        hasSameField = false;
                        continue;
                    }

                    innerClass.add(samePsiField.copy());
                }
            }
        }
    }

    private boolean containsPsiClass(ArrayList<PsiClass> psiClassList, PsiClass innerPsiClass) {
        for (PsiClass psiClass : psiClassList) {
            // 这里不会有第二级，参照Resp实体类生成规则
            if (psiClass.getName().equals(innerPsiClass.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsPsiField(ArrayList<PsiField> psiFieldList, PsiField psiField) {
        for (PsiField tField : psiFieldList) {
            if (isSameField(tField, psiField)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSameField(PsiField tField, PsiField psiField) {
        boolean result = Objects.equals(tField.getName(), psiField.getName())
                && tField.getType().toString().equals(psiField.getType().toString());
        Logc.d(result + " ; isSameField == [" + tField.getName() + " ; " + psiField.getName() + "] ; ["
                + tField.getType().toString() + " ; " + psiField.getType().toString() + "]");
        return result;
    }

}
