package info.narmontas.jpatterns.processor;

import info.narmontas.jpatterns.annotation.BuilderPattern;
import info.narmontas.jpatterns.annotation.BuilderPatternIgnore;
import info.narmontas.jpatterns.processor.utils.CodeGenerator;
import info.narmontas.jpatterns.processor.utils.Encapsulation;
import info.narmontas.jpatterns.processor.utils.Method;
import info.narmontas.jpatterns.processor.utils.Validator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builder Pattern processor.
 *
 * Creates simple Builder for POJO classes. elements from parent class will be used as well.
 */
@SupportedAnnotationTypes({"info.narmontas.jpatterns.annotation.BuilderPattern"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BuilderPatternProcessor extends AbstractProcessor {

    private Elements elementUtils;

    public final String listType = "java.util.List";
    public final String setType = "java.util.Set";
    public final String stringType = "java.lang.String";

    public final HashMap<String, String> defaultValues = new HashMap<String, String>() {{
            put(listType, " = new java.util.ArrayList<>()");
            put(setType, " = new java.util.HashSet<>()");
            put(stringType, " = \"\"");
    }};

    private CodeGenerator cg;
    private HashMap<String, String> collections;
    private HashMap<String, String> primitives;
    private HashMap<String, String> references;

    private String className;
    private String packageName;
    private String fullName;
    private String initialClassFullName;


    @Override
    public void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
    };

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(BuilderPattern.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> builderTemplateAnnotated = roundEnvironment
                .getElementsAnnotatedWith(BuilderPattern.class);

        Validator validator = new Validator(processingEnv);

        for (TypeElement type: ElementFilter.typesIn(builderTemplateAnnotated)) {
            validator.checkForNoArgumentConstructor(type);
        }

        for (Element element: builderTemplateAnnotated) {
            validator.checkIfElementIsPOJO(element);
        }

        for (Element element: builderTemplateAnnotated) {
            createClass(element);
        }

        return false;
    }

    private void createClass(Element element) {
        cg = new CodeGenerator();
        initNames(element);
        cg.definePackage(packageName);
        cg.defineClass(Encapsulation.PUBLIC, className);
        initFields(element);
        createFields();
        setCollectionAdders();
        HashMap<String, String> merged = merge();
        setSetters(merged);
        generateBuildMethod(merged);
        generateCreateMethod();
        generateFile();
    }


    // Helpers

    private void initFields(Element element) {
        collections = getCollections(element);
        primitives = getPrimitives(element);
        references = getReferences(element);
    }

    private void initNames(Element element) {
        className = element.getSimpleName().toString() + "Builder";
        List<PackageElement> packages =
                ElementFilter.packagesIn(Collections.singletonList(element.getEnclosingElement()));

        packageName = getPackageName(packages);

        fullName =
                getClassFullName(packageName, className);
        initialClassFullName =
                getClassFullName(packageName, element.getSimpleName().toString());
    }

    private void createFields() {
        setCollectionsFields();
        setPrimitivesFields();
        setReferenceFields();
    }

    private HashMap<String, String> merge() {
        return Stream.of(collections, primitives, references)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (map1, map2) -> map1,
                        HashMap::new));
    }



    // Getters

    private String getClassFullName(String packageName, String className) {
        if (packageName == null || packageName.isEmpty()) {
            return className;
        }
        return packageName + "." + className;
    }

    private String getPackageName(List<PackageElement> packages) {
        if (packages.size() == 0 || packages.get(0).isUnnamed()) {
            return null;
        } else {
            return packages.get(0).getQualifiedName().toString();
        }
    }

    private HashMap<String, String> getCollections(Element element) {
        TypeElement te = elementUtils.getTypeElement(element.asType().toString());
        return elementUtils.getAllMembers(te).stream()
                .filter(this::getterPredicate)
                .map(el -> (ExecutableElement) el)
                .filter(this::collectionPredicate)
                .collect(Collectors.toMap(
                        el -> el.getSimpleName().toString(),
                        el -> el.getReturnType().toString(),
                        (el1, el2) -> el1,
                        HashMap::new));
    }

    private HashMap<String, String> getPrimitives(Element element) {
        TypeElement te = elementUtils.getTypeElement(element.asType().toString());
        return elementUtils.getAllMembers(te).stream()
                .filter(this::getterPredicate)
                .map(el -> (ExecutableElement) el)
                .filter(el -> !collectionPredicate(el) && primitivePredicate(el))
                .collect(Collectors.toMap(
                        el -> el.getSimpleName().toString(),
                        el -> el.getReturnType().toString(),
                        (el1, el2) -> el1,
                        HashMap::new));
    }

    private HashMap<String, String> getReferences(Element element) {
        TypeElement te = elementUtils.getTypeElement(element.asType().toString());
        return elementUtils.getAllMembers(te).stream()
                .filter(this::getterPredicate)
                .map(el -> (ExecutableElement) el)
                .filter(el -> !collectionPredicate(el) && !primitivePredicate(el))
                .collect(Collectors.toMap(
                        el -> el.getSimpleName().toString(),
                        el -> el.getReturnType().toString(),
                        (el1, el2) -> el1,
                        HashMap::new));
    }

    private String getFieldName(String accessorName) {
        return accessorName.substring(3, 4).toLowerCase() +
                accessorName.substring(4);
    }

    private String getCollectionDefaultValue(String collectionType) {
        if (collectionType.startsWith(listType)) {
            return defaultValues.get(listType);
        } else if (collectionType.startsWith(setType)) {
            return defaultValues.get(setType);
        }
        return null;
    }

    private String getReferenceDefaultValue(String  referenceType) {
        if(referenceType.startsWith(stringType)) {
            return defaultValues.get(stringType);
        }
        return " = null";
    }


    // Setters

    private void setCollectionsFields() {
        collections.forEach((name, type) -> {
            cg.addField(Encapsulation.PRIVATE, type, getFieldName(name)
                    + getCollectionDefaultValue(type));
        });
    }

    private void setPrimitivesFields() {
        primitives.forEach((name, type) -> {
            cg.addField(Encapsulation.PRIVATE, type, getFieldName(name));
        });
    }

    private void setReferenceFields() {
        references.forEach((name, type) -> {
            cg.addField(Encapsulation.PRIVATE, type, getFieldName(name)
                    + getReferenceDefaultValue(type));
        });
    }

    private void setCollectionAdders() {
        collections.forEach((name, type) -> {
            Method method = new Method("add" + name.substring(3, name.length() -1))
                    .setEncapsulation(Encapsulation.PUBLIC)
                    .setReturnType(fullName)
                    .addParameter(extractGenericType(type), "item")
                    .addBodyLine("this." + getFieldName(name) + ".add(item);")
                    .addBodyLine("return this;");
            cg.addMethod(method);
        });
    }

    private String extractGenericType(String type) {
        int from = type.indexOf('<');
        int to = type.indexOf('>');
        if (from == -1 || to == -1) return null;
        return type.substring(from + 1, to);
    }

    private void setSetters(HashMap<String, String> methods) {
        methods.forEach((name, type) -> {
            Method method = new Method("set" + name.substring(3))
                    .setEncapsulation(Encapsulation.PUBLIC)
                    .setReturnType(fullName)
                    .addParameter(type, "obj")
                    .addBodyLine("this." + getFieldName(name) + " = obj;")
                    .addBodyLine("return this;");
            cg.addMethod(method);
        });

    }


    // Predicates

    private boolean getterPredicate(Element element) {
        return  element.getKind() == ElementKind.METHOD
                && element.getSimpleName().toString().startsWith("get")
                && !element.getSimpleName().toString().startsWith("getClass")
                && (element.getAnnotation(BuilderPatternIgnore.class) == null);
    }

    private boolean collectionPredicate(ExecutableElement element) {
        return element.getReturnType().toString().startsWith(listType)
                || element.getReturnType().toString().startsWith(setType);
    }

    private boolean primitivePredicate(ExecutableElement element) {
        return element.getReturnType().getKind().isPrimitive();
    }


    // Generators

    private void generateBuildMethod(HashMap<String, String> methods) {
        Method build = new Method("build")
                .setEncapsulation(Encapsulation.PUBLIC)
                .setReturnType(initialClassFullName)
                .addBodyLine(initialClassFullName + " obj = new " + initialClassFullName + "();");

        methods.forEach((name, type) -> {
            build.addBodyLine("obj.set" + name.substring(3) + "(" + getFieldName(name) + ");");
        });

        build.addBodyLine("return obj;");
        cg.addMethod(build);
    }

    private void generateCreateMethod() {
        Method builder = new Method("create")
                .setEncapsulation(Encapsulation.PUBLIC)
                .setStatic()
                .setReturnType(fullName)
                .addBodyLine("return new " + fullName + "();");
        cg.addMethod(builder);
    }

    private void generateFile() {
        try {
            JavaFileObject sourceFile = processingEnv.getFiler()
                    .createSourceFile(fullName);
            Writer writer = sourceFile.openWriter();
            writer.write(cg.build());
            writer.close();
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Cannot create source class.");
        }
    }
}
