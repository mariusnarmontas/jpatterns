package info.narmontas.jpatterns.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>BuilderPattern annotation can be added to POJO classes only.</p>
 * <p>POJOs that are annotated with @BuilderPattern will be provided
 * with POJOBuilder classes. Builders are generated in the same package
 * with names: POJO name + "Builder".</p>
 * <p>Ex., if annotated class name is "Person"
 * then builder class with the name "PersonBuilder" will be created.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface BuilderPattern {
}
