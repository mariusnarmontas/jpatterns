# JPatterns (Annotation Processing)
This plugin generates `Builder` source files during compilation. Builders can be generated for POJO class only.

The plugin is created for my projects and is used with JPA Entities.
*At the moment only Builder pattern is available* but in the future I hope to add more software design patterns.

It is simple to use: just annotate your POJO with `@BuilderPattern` annotation and after annotations are processed simple 
POJOBuilder class will be generated.

## Download

`JPatterns` jar file can be downloaded [here](http://narmontas.info/java_projects/patterns/jpatterns-0.1.0.jar).

## Usage example:

Jar file should be added as a dependency (Gradle example):
```groovy
dependencies {
    // ...
    implementation files('static_dependencies/jpatterns-0.1.0.jar')
    annotationProcessor files('static_dependencies/jpatterns-0.1.0.jar')
}
```

Then annotate your POJO class with `@BuilderPattern` annotation from `info.narmontas.jpatterns.annotation` package:
```java
package org.example.pojo;

import java.util.List;
import java.util.Set;
import info.narmontas.jpatterns.annotation.BuilderPattern;

@BuilderPattern
public class Person {
    private long id;
    private String name;
    private List<String> nickNames;
    private Set<String> socialNetworks;
    
    // getters and setters

    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    } 
    
    // ...

}
```

For the above stated POJO class Builder will be generated as follows:
```java
package org.example.pojo;

public class PersonBuilder {
    private long id;
    private java.lang.String name = "";
    private java.util.List<java.lang.String> nickNames = new java.util.ArrayList<>();
    private java.util.Set<java.lang.String> socialNetworks = new java.util.HashSet<>();

    public org.example.pojo.PersonBuilder addNickName(java.lang.String item) {
        this.nickNames.add(item);
        return this;
    }
    
    public org.example.pojo.PersonBuilder addSocialNetwork(java.lang.String item) {
        this.socialNetworks.add(item);
        return this;
    }

    public org.example.pojo.PersonBuilder setId(long obj) {
        this.id = obj;
        return this;
    }

    public org.example.pojo.PersonBuilder setName(java.lang.String obj) {
        this.name = obj;
        return this;
    }

    public org.example.pojo.PersonBuilder setNickNames(java.util.List<java.lang.String> obj) {
        this.setNickNames = obj;
    }

    public org.example.pojo.PersonBuilder setSocialNetworks(java.util.Set<java.lang.String> obj) {
        this.socialNetworks = obj;
    }

    public org.example.pojo.Person build() {
        org.example.pojo.Person obj = new org.example.pojo.Person();
        obj.setId(id);
        obj.setName(name);
        obj.setNickNames(nickNames);
        obj.setSocialNetworks(socialNetworks);
        return obj;
    }

    public static org.example.pojo.PersonBuilder create() {
        return new org.example.pojo.PersonBuilder();
    }
}
```

please note, default values for List, String and Set types are created. At the moment default values are created as follows:
- `java.util.List` (default value: `new java.util.ArrayList<>()`)
- `java.util.Set` (default value: `new java.util.HashSet<>()`)
- `java.lang.String` (default value: `""`);

For other reference types default values will be `null`. For primitive types no default values are assigned.

IMPORTANT: _If some fields should not be included to builder, you should annotate their getter methods with `@BuilderPatternIgnore` annotation._

Builder usage:
```
// ...
Person person = PersonBuilder.create()
    .setId(1L)
    .setName("John")
    .addNickName("john")
    .addSocialNetrwork("facebook")
    .build();
// ...
```
