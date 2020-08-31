package info.narmontas.patterns.processor.utils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.Set;
import java.util.stream.Collectors;

public class Validator {

    private final ProcessingEnvironment processingEnv;

    public Validator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public void checkForNoArgumentConstructor(TypeElement type) {
        for (ExecutableElement constructor: ElementFilter.constructorsIn(type.getEnclosedElements())) {
            if (constructor.getParameters().isEmpty()) return;
        }

        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR, "Missing no argument constructor", type);
    }

    /**
     * Checks if getters and setters are provided.
     */
    public void checkIfElementIsPOJO(Element element) {
        String elementName = element.asType().toString();
        TypeElement elementType = processingEnv.getElementUtils().getTypeElement(elementName);

        Set<? extends Element> setters =
                processingEnv.getElementUtils().getAllMembers(elementType)
                .stream()
                .filter(elem -> elem.getKind() == ElementKind.METHOD &&
                        elem.getSimpleName().toString().startsWith("set"))
                .collect(Collectors.toSet());

        Set<? extends Element> getters =
                processingEnv.getElementUtils().getAllMembers(elementType)
                .stream()
                .filter(elem -> elem.getKind() == ElementKind.METHOD &&
                        elem.getSimpleName().toString().startsWith("get") &&
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
                                .equals(getter.getSimpleName().toString().substring(3)) &&
                                ((ExecutableElement) setter).getParameters().size() == 1
                    ).count() == 1)
                .count();

        if (samples != getters.size() && samples != setters.size()) {
            processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.ERROR,
                    "Class " + element.toString() + " is not POJO.");
        }
    }
}
