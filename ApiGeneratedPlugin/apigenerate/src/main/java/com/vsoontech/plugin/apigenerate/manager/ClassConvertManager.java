package com.vsoontech.plugin.apigenerate.manager;


import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import com.vsoontech.plugin.apigenerate.Config;
import com.vsoontech.plugin.apigenerate.entity.ApiDetail;
import com.vsoontech.plugin.apigenerate.entity.EntityClass;
import com.vsoontech.plugin.apigenerate.entity.EntityField;
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

class ClassConvertManager {

    private final String REQ_CLASS_PATH = "/META-INF/ReqClass.tmpl";
    private final String RESP_CLASS_PATH = "/META-INF/RespClass.tmpl";
    private final String INNER_CLASS_PATH = "/META-INF/InnerClass.tmpl";
    private final String CONSTRUCT_METHOD_PATH = "/META-INF/ConstructMethod.tmpl";
    private String reqClassTemplate;
    private String respClassTemplate;
    private String innerClassTemplate;
    private String constructMethodTemplate;
    private String targetPackageName;
    private File mApiJavaDir;
    private Formatter mFormatter;

    public File[] getApiJavaFiles() {
        return mApiJavaDir != null ? mApiJavaDir.listFiles() : null;
    }


    ClassConvertManager(String javaOutputDir, String appPackageName) {
        mFormatter = new Formatter();
        targetPackageName = Config.genOutSrc;

        reqClassTemplate = getResourceAsStream(REQ_CLASS_PATH);
        respClassTemplate = getResourceAsStream(RESP_CLASS_PATH);
        innerClassTemplate = getResourceAsStream(INNER_CLASS_PATH);
        constructMethodTemplate = getResourceAsStream(CONSTRUCT_METHOD_PATH);

        // 检查 / 创建Java文件输出文件夹
        String apiJavaOutputDir = (javaOutputDir + File.separator + targetPackageName)
            .replace(".", File.separator) + File.separator;
        mApiJavaDir = new File(apiJavaOutputDir);
        if (!mApiJavaDir.exists() && mApiJavaDir.mkdirs()) {
            Logc.d(mApiJavaDir.toString());
        }

    }

    private String getResourceAsStream(String filePath) {
        InputStream is = ClassConvertManager.class.getResourceAsStream(filePath);
        Scanner scanner = new Scanner(is);
        return scanner.useDelimiter("\\A").next();
    }

    void generate(ApiDetail apiDetail) throws IOException, FormatterException {

        if (apiDetail != null
            && mApiJavaDir != null
            && mApiJavaDir.exists()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
            // Req
            if (apiDetail.reqEntityCls != null
                && !isEmpty(apiDetail.reqEntityCls.className)) {
                EntityClass tCls = apiDetail.reqEntityCls;
                File reqJavaFile = new File(mApiJavaDir, tCls.className + ".java");
                String reqJavaContent = reqClassTemplate
                    .replaceAll("%package%", targetPackageName)
                    .replaceAll("%logger%", checkLogger(tCls.getInnerClss()))
                    .replaceAll("%innerClass%", createInnerClassContent(tCls.getInnerClss()))
                    .replaceAll("%construct%", createConstructContent(tCls))
                    .replaceAll("%httpUrl%", apiDetail.httpUrl)
                    .replaceAll("%secondDomain%", apiDetail.getSecondDomain())
                    .replaceAll("%createData%", dateFormat.format(new Date()))
                    .replaceAll("%classDesc%", tCls.classDesc)
                    .replaceAll("%className%", tCls.className)
                    .replaceAll("%fieldContent%", createFieldContent(tCls.getFields()))
                    .replaceAll("%toString%", createToStringContent(tCls.className, tCls.getFields()));

                FileUtils.writeStringToFile(reqJavaFile,
                    mFormatter.formatSource(reqJavaContent),
                    Charsets.UTF_8, false);

                Logc.d(apiDetail.reqEntityCls.className + " UP-TO-DATE");
            }

            // Resp
            if (apiDetail.respEntityCls != null
                && !isEmpty(apiDetail.respEntityCls.className)) {
                EntityClass tCls = apiDetail.respEntityCls;
                File respJavaFile = new File(mApiJavaDir, tCls.className + ".java");
                String respJavaContent = respClassTemplate
                    .replaceAll("%package%", targetPackageName)
                    .replaceAll("%createData%", dateFormat.format(new Date()))
                    .replaceAll("%classDesc%", tCls.classDesc)
                    .replaceAll("%className%", tCls.className)
                    .replaceAll("%fieldContent%", createFieldContent(tCls.getFields()))
                    .replaceAll("%innerClass%", createInnerClassContent(tCls.getInnerClss()))
                    .replaceAll("%toString%", createToStringContent(tCls.className, tCls.getFields()))
                    .replaceAll("%generateModelDesc%", apiDetail.getSampleDesc());

                FileUtils.writeStringToFile(respJavaFile,
                    mFormatter.formatSource(respJavaContent),
                    Charsets.UTF_8, false);
                Logc.d(apiDetail.respEntityCls.className + " UP-TO-DATE");
            }
        } else {
            Logc.d("apiDetail or mApiJavaDir is null");
        }

    }

    private String createConstructContent(EntityClass cls) {
        if (cls != null
            && cls.getInnerClss() != null
            && !cls.getInnerClss().isEmpty()) {
            EntityClass innerClass = cls.getInnerClss().get(0);
            String newCls = constructMethodTemplate
                .replaceAll("%className%", cls.className)
                .replaceAll("%innerClassName%", innerClass.className);
            return newCls;
        }

        return "";
    }

    private String checkLogger(ArrayList<EntityClass> innerClss) {
        return innerClss != null && !innerClss.isEmpty() ? "import com.linkin.base.debug.logger.L;" : "";
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
            String newCls = innerClassTemplate
                .replaceAll("%className%", cls.className)
                .replaceAll("%fieldContent%", createFieldContent(cls.getFields()))
                .replaceAll("%toString%", createToStringContent(cls.className, cls.getFields()));
            return newCls;
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
                    builder.append("// ")
                        .append(field.desc)
                        .append(" ; NotNull : ")
                        .append(field.notNull).append("\n");
                    // public int total;
                    builder.append("public").append(" ")
                        .append(field.type.value(field.target)).append(" ")
                        .append(field.name).append(";\n\n");
                }
            }
        }
        return builder.toString();
    }

    private boolean isEmpty(CharSequence s) {
        if (s == null) {
            return true;
        } else {
            return s.length() == 0;
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