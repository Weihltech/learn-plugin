package com.vsoontech.plugin.apigenerate.manager;


import static com.vsoontech.plugin.apigenerate.ApiConfig.isEmpty;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.vsoontech.plugin.apigenerate.ApiConfig;
import com.vsoontech.plugin.apigenerate.entity.ApiDetail;
import com.vsoontech.plugin.apigenerate.entity.EntityClass;
import com.vsoontech.plugin.apigenerate.entity.EntityField;
import com.vsoontech.plugin.apigenerate.entity.EntityField.FieldLink;
import com.vsoontech.plugin.apigenerate.utils.Logc;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;

// 根据基础信息，转义成 java 文件；即Resp、Req 实体类
class ClassConvertManager {

    private String reqClassTemplate;
    private String respClassTemplate;
    private String innerClassTemplate;
    private String constructMethodTemplate;
    private String constsClassTemplate;
    private File mApiJavaDir;
    private Formatter mFormatter;
    private String targetPackageName;
    private SimpleDateFormat dateFormat;
    private String tConstsClassName;


    ClassConvertManager() {
        mFormatter = new Formatter();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
        reqClassTemplate = getResourceAsStream(ApiConfig.TMPL_REQ_CLASS_PATH);
        respClassTemplate = getResourceAsStream(ApiConfig.TMPL_RESP_CLASS_PATH);
        innerClassTemplate = getResourceAsStream(ApiConfig.TMPL_INNER_CLASS_PATH);
        constructMethodTemplate = getResourceAsStream(ApiConfig.TMPL_CONSTRUCT_METHOD_PATH);
        constsClassTemplate = getResourceAsStream(ApiConfig.TMPL_CONSTS_CLASS_PATH);
        mApiJavaDir = new File(ApiConfig.getApiJavaOutputDir());
        targetPackageName = ApiConfig.sProp.genOutSrc;

        if (!mApiJavaDir.exists() && mApiJavaDir.mkdirs()) {
            Logc.d("Create Api Java-OutPuts-Dir Success !");
        }
    }

    private String getResourceAsStream(String filePath) {
        InputStream is = ClassConvertManager.class.getResourceAsStream(filePath);
        Scanner scanner = new Scanner(is);
        return scanner.useDelimiter("\\A").next();
    }

    void logStart() {
        if (openLog()) {
            Logc.d("");
            Logc.d("@ ClassConvertManager <-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-<-");
        }
    }

    void generate(ApiDetail apiDetail) throws IOException, FormatterException {

        if (openLog()) {
            Logc.d("Convert Target [" + apiDetail.toString() + "] ");
        }
        if (apiDetail != null
            && mApiJavaDir != null
            && mApiJavaDir.exists()) {
            tConstsClassName = apiDetail.respEntityCls.className
                .replace(ApiConfig.RESP, ApiConfig.CONSTS);

            // Req
            createReq(apiDetail);

            // Resp
            createResp(apiDetail);

            // constsJavaFile
            createConsts(apiDetail);
        } else {
            if (openLog()) {
                Logc.d("ApiDetail Or Api-Java-OutPuts-Dir Is Null !");
            }
        }

    }

    private void createConsts(ApiDetail apiDetail) throws IOException, FormatterException {
        if (!isEmpty(tConstsClassName)
            && tConstsClassName.endsWith(ApiConfig.CONSTS)) {
            StringBuilder constsBuilder = new StringBuilder();
            ArrayList<String> linkArr = new ArrayList<>();
            collectEntityClassLinks(linkArr, constsBuilder, apiDetail.reqEntityCls);
            collectEntityClassLinks(linkArr, constsBuilder, apiDetail.respEntityCls);
            if (constsBuilder.length() > 0) {
                File constsJavaFile = new File(mApiJavaDir, tConstsClassName + ApiConfig.JAVA_SUFFIX);
                String constsContent = constsClassTemplate
                    .replaceAll("%package%", targetPackageName)
                    .replaceAll("%createData%", dateFormat.format(new Date()))
                    .replaceAll("%classDesc%", apiDetail.desc + " --> 常量")
                    .replaceAll("%className%", tConstsClassName)
                    .replaceAll("%constsContent%", constsBuilder.toString())
                    .replaceAll("%generateModelDesc%", apiDetail.getSampleDesc());
                try {
                    FileUtils.writeStringToFile(constsJavaFile,
                        mFormatter.formatSource(constsContent),
                        Charsets.UTF_8, false);
                } catch (FormatterException e) {
                    FileUtils.writeStringToFile(
                        new File(constsJavaFile.getParent(), "Error" + constsJavaFile.getName()),
                        constsContent,
                        Charsets.UTF_8, false);
                    throw new FormatterException("Illegal-Java-File ! 详细查看Java输出文件");
                }
                if (openLog()) {
                    Logc.d(tConstsClassName + " UP-TO-DATE");
                }
            }
        }
    }

    private void collectEntityClassLinks(ArrayList<String> linkArr, StringBuilder constsBuilder,
        EntityClass entityCls) {
        if (entityCls != null
            && !entityCls.getFields().isEmpty()) {
            for (EntityField entityField : entityCls.getFields()) {
                if (entityField.type.isEnum()
                    && entityField.links != null
                    && !entityField.links.isEmpty()) {
                    for (FieldLink link : entityField.links) {
                        // 去重
                        if (!linkArr.contains(link.name)) {
                            // desc
                            constsBuilder.append(" // ").append(link.desc).append("\n");
                            // consts
                            String type = link.type.toLowerCase();
                            if ("int".equals(type)) {
                                constsBuilder.append("public static final int ")
                                    .append(link.name).append(" = ")
                                    .append(link.value).append(";\n");
                            } else if ("string".equals(type)) {
                                constsBuilder.append("public static final String ")
                                    .append(link.name).append(" = \"")
                                    .append(link.value).append("\";\n\n");
                            }
                            linkArr.add(link.name);
                        }
                    }
                }
            }
        }

        // 递推收集内部类常量
        ArrayList<EntityClass> innerClass = entityCls.getInnerClss();
        if (!innerClass.isEmpty()) {
            for (EntityClass innerCls : innerClass) {
                collectEntityClassLinks(linkArr, constsBuilder, innerCls);
            }
        }
    }

    private void createResp(ApiDetail apiDetail) throws IOException, FormatterException {
        if (apiDetail.respEntityCls != null
            && !isEmpty(apiDetail.respEntityCls.className)) {
            EntityClass tCls = apiDetail.respEntityCls;
            File respJavaFile = new File(mApiJavaDir, tCls.className + ApiConfig.JAVA_SUFFIX);
            String respJavaContent = respClassTemplate
                .replaceAll("%package%", targetPackageName)
                .replaceAll("%createData%", dateFormat.format(new Date()))
                .replaceAll("%classDesc%", tCls.classDesc)
                .replaceAll("%className%", tCls.className)
                .replaceAll("%fieldContent%", createFieldContent(tCls.getFields()))
                .replaceAll("%innerClass%", createInnerClassContent(tCls.getInnerClss()))
                .replaceAll("%toString%", createToStringContent(tCls.className, tCls.getFields()))
                .replaceAll("%generateModelDesc%", apiDetail.getSampleDesc());

            try {
                FileUtils.writeStringToFile(respJavaFile,
                    mFormatter.formatSource(respJavaContent),
                    Charsets.UTF_8, false);
            } catch (FormatterException e) {
                FileUtils.writeStringToFile(
                    new File(respJavaFile.getParent(), "Error" + respJavaFile.getName()),
                    respJavaContent,
                    Charsets.UTF_8, false);
                throw new FormatterException("Illegal-Java-File ! 详细查看Java输出文件");
            }
            if (openLog()) {
                Logc.d(apiDetail.respEntityCls.className + " UP-TO-DATE");
            }
        }
    }

    private void createReq(ApiDetail apiDetail) throws IOException, FormatterException {
        if (apiDetail.reqEntityCls != null
            && !isEmpty(apiDetail.reqEntityCls.className)) {
            EntityClass tCls = apiDetail.reqEntityCls;
            File reqJavaFile = new File(mApiJavaDir, tCls.className + ApiConfig.JAVA_SUFFIX);
            String reqJavaContent = reqClassTemplate
                .replaceAll("%package%", targetPackageName)
                .replaceAll("%loggerImport%", checkLogger(tCls.getInnerClss()))
                .replaceAll("%innerClass%", createInnerClassContent(tCls.getInnerClss()))
                .replaceAll("%construct%", createReqConstructContent(tCls))
                .replaceAll("%httpUrl%", apiDetail.httpUrl)
                .replaceAll("%secondDomain%", apiDetail.getSecondDomain())
                .replaceAll("%createData%", dateFormat.format(new Date()))
                .replaceAll("%classDesc%", tCls.classDesc)
                .replaceAll("%className%", tCls.className)
                .replaceAll("%type%", apiDetail.getHttpType())
                .replaceAll("%httpTypeImport%", apiDetail.getHttpTypeImport())
                .replaceAll("%fieldContent%", createFieldContent(tCls.getFields()))
                .replaceAll("%toString%", createToStringContent(tCls.className, tCls.getFields()));

            try {
                FileUtils.writeStringToFile(reqJavaFile,
                    mFormatter.formatSource(reqJavaContent),
                    Charsets.UTF_8, false);
            } catch (FormatterException e) {
                FileUtils.writeStringToFile(
                    new File(reqJavaFile.getParent(), "Error" + reqJavaFile.getName()),
                    reqJavaContent,
                    Charsets.UTF_8, false);
                throw new FormatterException("Illegal-Java-File ! 详细查看Java输出文件");
            }

            if (openLog()) {
                Logc.d(apiDetail.reqEntityCls.className + " UP-TO-DATE");
            }
        }
    }

    private String createReqConstructContent(EntityClass cls) {
        if (cls != null
            && cls.getInnerClss() != null
            && !cls.getInnerClss().isEmpty()) {
            for (EntityClass innerClass : cls.getInnerClss()) {
                if ("Params".toLowerCase().equals(innerClass.className.toLowerCase())) {
                    return constructMethodTemplate
                        .replaceAll("%className%", cls.className)
                        .replaceAll("%innerClassName%", innerClass.className);
                }
            }
        }

        return "";
    }

    private String checkLogger(ArrayList<EntityClass> innerClss) {
//        return innerClss != null && !innerClss.isEmpty() ? ApiConfig.IMPORT_LOGGER : "";
        return "";
    }

    private String createInnerClassContent(ArrayList<EntityClass> innerClss) {
        StringBuilder builder = new StringBuilder();
        if (innerClss != null && !innerClss.isEmpty()) {
            for (EntityClass cls : innerClss) {
                if (cls != null) {
                    builder.append(newInnerClsContent(cls)).append("\n\n");
                }
            }
        }
        return builder.toString();
    }

    private String newInnerClsContent(EntityClass cls) {

        if (cls != null && !cls.getFields().isEmpty()) {
            return innerClassTemplate
                .replaceAll("%className%", cls.className)
                .replaceAll("%fieldContent%", createFieldContent(cls.getFields()))
                .replaceAll("%toString%", createToStringContent(cls.className, cls.getFields()));
        }

        return "";
    }

    private String createToStringContent(String className, ArrayList<EntityField> fields) {
        StringBuilder builder = new StringBuilder();
        if (fields != null && !fields.isEmpty()) {
            builder.append(className).append("{\" +\n");
            for (EntityField field : fields) {
                if (field != null) {
                    builder.append("\": ")
                        .append(field.name).append("=\" + ")
                        .append(field.name).append(" +\n");
                }
            }
            builder.append("\"}");
        }
        return builder.length() > 0 ? builder.toString() : (className + "{}");
    }

    private String createFieldContent(ArrayList<EntityField> fields) {
        StringBuilder builder = new StringBuilder();
        if (fields != null && !fields.isEmpty()) {
            for (EntityField field : fields) {
                if (field != null) {
                    // desc
                    String desc = field.desc.replaceAll("\\n", "\\n//");
                    if (field.type.isEnum()) {
                        /**
                         * {@link AddressReq.Params}
                         */
                        builder.append("/** ").append(desc).append("<br/>");
                        for (FieldLink link : field.links) {
                            builder.append(link.desc).append(": {@link ").append(tConstsClassName).append("#")
                                .append(link.name)
                                .append("} ; <br/>");
                        }
                        builder.append("*/").append("\n");
                    } else {
                        builder.append("// ").append(desc).append("\n");
                    }

                    // public int total;
                    builder.append("public").append(" ")
                        .append(field.type.value(field.target)).append(" ")
                        .append(field.name).append(";\n\n");
                }
            }
        }
        return builder.toString();
    }

    private boolean openLog() {
        return ApiConfig.sProp == null || ApiConfig.sProp.openLog || ApiConfig.sProp.openConvertLog;
    }

    void cleanApiJavaDir() {
        try {
            Logc.d("[Clean JavaFiles .]  " + mApiJavaDir.getAbsolutePath());
            FileUtils.cleanDirectory(mApiJavaDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

//    根据反射，对 cls 对象赋值
//    private static Object objAssignModel(Object obj, Class cls) throws InstantiationException, IllegalAccessException {
////        Test t = new Test();
////        Field f = t.getClass().getDeclaredField("name");
////        f.setAccessible(true);
////        f.set(t, "this is test1");
////        System.out.println(t.getName());
//
//        Object modelCls = cls.newInstance();
//        Field[] modelFields = cls.getFields();
//        if (modelFields.length > 0) {
//            Field[] fields = obj.getClass().getFields();
//            if (fields.length > 0) {
//                for (Field field : fields) {
//                    field.getName();
//                    field.getType();
//                    Field modelField = findModelField(field, modelFields);
//                    if (modelField != null) {
//                        modelField.setAccessible(true);
//                        modelField.set(modelCls, field.get(obj));
//                    }
//                }
//            }
//            return modelCls;
//        }
//
//        return null;
//    }
//
//    private static Field findModelField(Field field, Field[] modelFields) {
//        for (Field modeField : modelFields) {
//            System.out.println("======= " + modeField.getGenericType().toString());
//            if (modeField.getName().equals(field.getName())
//                && modeField.getType().getName().equals(field.getType().getName())) {
//                return modeField;
//            }
//        }
//        return null;
//    }