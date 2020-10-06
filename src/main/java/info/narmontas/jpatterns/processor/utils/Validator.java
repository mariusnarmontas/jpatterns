package info.narmontas.jpatterns.processor.utils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validator class provides tools for class inspections before
 * annotation processing is started.
 */
public class Validator {

    private final ProcessingEnvironment processingEnv;

    public Validator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    /**
     * Checks if checked element has No Argument Constructor
     * @param type TypeElement
     */
    public void checkForNoArgumentConstructor(TypeElement type) {
        for (ExecutableElement constructor: ElementFilter.constructorsIn(type.getEnclosedElements())) {
            if (constructor.getParameters().isEmpty()) return;
        }

        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR, "Missing no argument constructor", type);
    }

    /**
     * Checks if provided name begins with provided type
     * plus upper case letter.
     * @param name String name, ex., getName
     * @param type String getter type ex., get
     * @return boolean
     */
    public boolean getterOrSetterTypePredicate(String name, String type) {
        return name.startsWith(type) &&
                name.substring(type.length(), type.length() + 1)
                        .matches("[A-Z]");
    }

    /**
     * Checks if element has getters and equal amount of setters for the same parameters.
     * @param element Element
     */
    public void checkIfElementIsPOJO(Element element) {
        String elementName = element.asType().toString();
        TypeElement elementType = processingEnv.getElementUtils().getTypeElement(elementName);

        Set<? extends Element> setters =
                processingEnv.getElementUtils().getAllMembers(elementType)
                .stream()
                .filter(elem -> elem.getKind() == ElementKind.METHOD &&
                        getterOrSetterTypePredicate(elem.getSimpleName().toString(), "set"))
                .collect(Collectors.toSet());

        Set<? extends Element> getters =
                processingEnv.getElementUtils().getAllMembers(elementType)
                .stream()
                .filter(elem -> elem.getKind() == ElementKind.METHOD &&
                        (getterOrSetterTypePredicate(elem.getSimpleName().toString(), "get")
                        || getterOrSetterTypePredicate(elem.getSimpleName().toString(), "has")
                        || getterOrSetterTypePredicate(elem.getSimpleName().toString(), "is")) &&
                        !elem.getSimpleName().toString().startsWith("getClass"))
                .collect(Collectors.toSet());

        checkIfPOJO(setters, getters, element);
    }

    private void checkIfPOJO(Set<? extends Element> setters,
                             Set<? extends Element> getters,
                             Element element) {

        long samples = getters.stream()
                .filter(getter -> setters.stream().filter(setter ->
                        setter.getSimpleName().toString().substring(3)
                                .equals(getSubstring(getter.getSimpleName().toString())) &&
                                ((ExecutableElement) setter).getParameters().size() == 1
                    ).count() == 1)
                .count();

        if (samples != getters.size() && samples != setters.size()) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Class " + element.toString() + " is not POJO.");
        }
    }

    public String getSubstring(String name) {
        if (getterOrSetterTypePredicate(name, "get")
                || getterOrSetterTypePredicate(name, "has"))
            return name.substring(3);
        if (getterOrSetterTypePredicate(name, "is"))
            return name.substring(2);
        return name;
    }
}
