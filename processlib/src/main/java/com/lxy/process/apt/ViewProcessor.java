package com.lxy.process.apt;

import com.google.auto.service.AutoService;
import com.lxy.process.anno.BindView;
import com.lxy.process.anno.TargetClass;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * @author a
 * 自定义注解处理器
 */
@AutoService(Processor.class)
public class ViewProcessor extends AbstractProcessor {

    private Elements elementUtils;

    /**
     * 该初始化方法会被注解处理工具调用，并传入参数processingEnvironment，
     * 该参数提供了很多有用的工具类，例如Elements、Types、Filter等等
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        elementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        // 添加自定义注解 请务必注意set的添加顺序
        set.add(TargetClass.class.getCanonicalName());
        set.add(BindView.class.getCanonicalName());

        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }

    /**
     * process逻辑  就是自动生成代码的地方
     *
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        // 获取所有注解了BindView 的elements
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(TargetClass.class);

        for (Element element : elements) {
            // 注解元素的外侧元素，即 View 的所在 Activity 类
            TypeElement activityElement = (TypeElement) element;

            // 创建方法 需要引入 javapoet
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(TypeName.VOID)
                    .addParameter(ClassName.get(activityElement), "activity");

            //  获取单个类中，所有子Element
            List<? extends Element> allMembers = elementUtils.getAllMembers(activityElement);
            for (Element fieldElement : allMembers) {
                BindView bindView = fieldElement.getAnnotation(BindView.class);
                if (bindView != null) {
                    // 获取resId
                    int resID = bindView.value();
                    // 添加代码
                    methodBuilder.addStatement(
                            "activity.$L= ($T) activity.findViewById($L)",
                            fieldElement,
                            ClassName.get(fieldElement.asType()),
                            resID
                    );
                }
            }
            MethodSpec methodSpec = methodBuilder.build();
            // 创建类
            TypeSpec typeSpec = TypeSpec.classBuilder("View" + activityElement.getSimpleName())
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(methodSpec)
                    .build();
            try {
                // 获取包名
                PackageElement packageElement = elementUtils.getPackageOf(activityElement);
                String packageName = packageElement.getQualifiedName().toString();
                // 创建文件
                JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
                javaFile.writeTo(processingEnv.getFiler());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return false;
    }

}
