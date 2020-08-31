package info.narmontas.patterns;

import info.narmontas.patterns.processor.utils.CodeGenerator;
import info.narmontas.patterns.processor.utils.Encapsulation;
import info.narmontas.patterns.processor.utils.Method;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CodeGeneratorTest {

    @Test
    public void testGeneratedClassWithParametersOnly() {
        String expectedClass =
                "package org.test;\n\n" +
                "import java.lang.String;\n" +
                "import java.util.List;\n\n" +
                "public class TestClass {\n" +
                    "\tpublic String name;\n" +
                    "\tpublic List<String> names;\n" +
                "}";
        CodeGenerator cg = new CodeGenerator();
        String actualClass = cg.definePackage("org.test")
                .addImport("java.lang.String")
                .addImport("java.util.List")
                .defineClass(Encapsulation.PUBLIC, "TestClass")
                .addField(Encapsulation.PUBLIC, "String", "name")
                .addField(Encapsulation.PUBLIC, "List<String>", "names")
                .build();
        assertEquals(expectedClass, actualClass);
    }

    @Test
    public void testLiteralsWithSpace() {
        CodeGenerator cg = new CodeGenerator();
        assertThrows(IllegalArgumentException.class,
                () -> cg.definePackage("wrong literal"));
    }

    @Test
    public void testWithMethods() {
        String expectedClass =
                "package org.test;\n\n" +
                "import java.lang.String;\n" +
                "import java.util.List;\n\n" +
                "public class TestWithMethodClass {\n" +
                    "\tprivate String name;\n" +
                    "\tprivate List<String> names;\n" +
                    "\n\tpublic String getName() {\n" +
                        "\t\treturn name;\n" +
                    "\t}\n" +
                    "\n\tpublic void setName(String name, String name1) {\n" +
                        "\t\tif (name != null) {\n" +
                            "\t\t\tthis.name = name;\n" +
                            "\t\t\tSystem.out.println(name1);\n" +
                        "\t\t}\n" +
                    "\t}\n" +
                "}";

        CodeGenerator cg = new CodeGenerator();
        Method getName =
                new Method("getName")
                        .setEncapsulation(Encapsulation.PUBLIC)
                        .setReturnType("String")
                        .addBodyLine("return name;");

        Method setName =
                new Method("setName")
                        .setEncapsulation(Encapsulation.PUBLIC)
                        .addParameter("String", "name")
                        .addParameter("String", "name1")
                        .addBodyLineAndOpenBlock("if (name != null)")
                        .addBodyLine("this.name = name;")
                        .addBodyLine("System.out.println(name1);");

        String actualClass = cg.definePackage("org.test")
                .addImport("java.lang.String")
                .addImport("java.util.List")
                .defineClass(Encapsulation.PUBLIC, "TestWithMethodClass")
                .addField(Encapsulation.PRIVATE, "String", "name")
                .addField(Encapsulation.PRIVATE, "List<String>", "names")
                .addMethod(getName)
                .addMethod(setName)
                .build();

        assertEquals(expectedClass, actualClass);
    }

    @Test
    public void testAutoBlockInClassClosings() {
        String expectedClass =
                "package org.test;\n\n" +
                "\npublic class ALotOfStaticBlocksTest {\n" +
                    "\tstatic {\n" +
                        "\t\tstatic {\n" +
                            "\t\t\tstatic {\n" +
                                "\t\t\t\tSystem.out.println(\"Hello\");\n" +
                            "\t\t\t}\n" +
                        "\t\t}\n" +
                    "\t}\n" +
                "}";

        CodeGenerator cg = new CodeGenerator();
        String actualClass = cg.definePackage("org.test")
                .defineClass(Encapsulation.PUBLIC, "ALotOfStaticBlocksTest")
                .addCustomCodeAndOpenBlock("static")
                .addCustomCodeAndOpenBlock("static")
                .addCustomCodeAndOpenBlock("static")
                .addCustomLine("System.out.println(\"Hello\");")
                .build();

        assertEquals(expectedClass, actualClass);
    }

    @Test
    public void testAutoBlockMixedClosings() {
        String expectedClass =
                "package org.test;\n\n" +
                "\npublic class ALotOfStaticBlocksWithMethodsTest {\n" +
                    "\tstatic {\n" +
                        "\t\tstatic {\n" +
                            "\n\t\t\tpublic String getName() {\n" +
                                "\t\t\t\treturn name;\n" +
                            "\t\t\t}\n" +
                            "\t\t\tstatic {\n" +
                                "\t\t\t\tSystem.out.println(\"Hello\");\n" +
                            "\t\t\t}\n" +
                            "\n\t\t\tpublic String getName1() {\n" +
                                "\t\t\t\treturn name1;\n" +
                            "\t\t\t}\n" +
                        "\t\t}\n" +
                    "\t}\n" +
                "}";

        CodeGenerator cg = new CodeGenerator();
        Method testGetter1 =
                new Method("getName")
                        .setEncapsulation(Encapsulation.PUBLIC)
                        .setReturnType("String")
                        .addBodyLine("return name;");

        Method testGetter2 =
                new Method("getName1")
                        .setEncapsulation(Encapsulation.PUBLIC)
                        .setReturnType("String")
                        .addBodyLine("return name1;");

        String actualClass = cg.definePackage("org.test")
                .defineClass(Encapsulation.PUBLIC, "ALotOfStaticBlocksWithMethodsTest")
                .addCustomCodeAndOpenBlock("static")
                .addCustomCodeAndOpenBlock("static")
                .addMethod(testGetter1)
                .addCustomCodeAndOpenBlock("static")
                .addCustomLine("System.out.println(\"Hello\");")
                .closeBlock()
                .addMethod(testGetter2)
                .build();

        assertEquals(expectedClass, actualClass);
    }
}
