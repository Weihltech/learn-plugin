package com.vsoontech.plugin.anno;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

//@AutoService(Processor.class)//自动注册
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AnnotationProcessor extends AbstractProcessor {

    private static final String SUFFIX = "$$ApiLifeCycle";
    private static final String APPLICATION_TEMPLATE_PATH = "/ApiAnno.tmpl";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
//        mElementUtils = processingEnv.getElementUtils();
//        mMessager = processingEnv.getMessager();
//        mFiler = processingEnv.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> supportedAnnotationTypes = new LinkedHashSet<>();

        supportedAnnotationTypes.add(ApiLifeCycle.class.getName());

        return supportedAnnotationTypes;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
//        // 类名和包名
//        FieldSpec fieldSpec = FieldSpec.builder(TypeName.CHAR, "name", Modifier.PUBLIC).build();
//        TypeSpec finderClass = TypeSpec.classBuilder("GeneratedClass")
//            .addModifiers(Modifier.PUBLIC)
//            .addField(fieldSpec)
//            .build();
//        // 创建Java文件
//        JavaFile javaFile = JavaFile.builder("com.vsoontech.plugin.api", finderClass).build();
//
//        try {
//            javaFile.writeTo(mFiler);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        processDefaultLifeCycle(roundEnvironment.getElementsAnnotatedWith(ApiLifeCycle.class));
        return true;
    }


    private void processDefaultLifeCycle(Set<? extends Element> elements) {
        // ApiLifeCycle
        for (Element e : elements) {
            ApiLifeCycle ca = e.getAnnotation(ApiLifeCycle.class);

            final String fileContent = "package com.vsoontech.plugin.api;\n"
                + "\n"
                + "public class Test {\n"
                + "\n"
                + "}";
            try {
                JavaFileObject fileObject = processingEnv.getFiler()
                    .createSourceFile("com.vsoontech.plugin.Test");
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Creating " + fileObject.toUri());
                Writer writer = fileObject.openWriter();
                try {
                    PrintWriter pw = new PrintWriter(writer);
                    pw.print(fileContent);
                    pw.flush();

                } finally {
                    writer.close();
                }
            } catch (IOException x) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, x.toString());
            }
        }
    }

}
