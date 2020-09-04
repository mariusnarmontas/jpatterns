package info.narmontas.jpatterns.processor.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * <p>Code generator class provides tools for Class content generation.
 * Result of the tools is String object.
 * Tools are organized in accordance with builder pattern.</p>
 * <p>Ex., (CodeGenerator instance)<br>
 *          .definePackage("package.canonical.name")<br>
 *          .addImport("java.util.List")<br>
 *          .defineClass(Encapsulation.PUBLIC, "ClassName")<br>
 *          .addField(Encapsulation.PRIVATE, "fieldName")<br>
 *          .build();</p>
 * <p>build() method returns String result with formatted class content.</p>
 */
public class CodeGenerator {
    public static final String LINE_BREAK = System.getProperty("line.separator");
    public static final String SPACE = " ";
    public static final String SEMI_COL = ";";
    public static final String TAB = "\t";
    public static final String BLOCK_OPEN = " {";
    public static final String BLOCK_CLOSE = "}";
    public static final String PAC = "package ";
    public static final String IMP = "import ";

    static String getEncapsulation(Encapsulation encapsulation) {
        switch (encapsulation) {
            case PUBLIC:
                return "public ";
            case PRIVATE:
                return "private ";
            case PROTECTED:
                return "protected ";
            case PACKAGE:
            case NONE:
                return "";
            default:
                throw new IllegalArgumentException("Wrong encapsulation argument.");
        }
    }

    private int closingsRequired = 0;
    private final StringBuilder builder = new StringBuilder();
    private HashMap<String, String> fields = new LinkedHashMap<>();

    /**
     * Class package should be provided as String packageName parameter.
     * @param packageName String
     * @return CodeGenerator
     */
    public CodeGenerator definePackage(String packageName) {
        if (isValid(packageName)) {
            append(PAC);
            append(packageName);
            endLine();
            append(LINE_BREAK);
        }
        return this;
    }

    /**
     * Add import line. Ex., addImport("java.util.List") will add
     * "import java.util.List;" line to the class.
     * @param importPackage String
     * @return CodeGenerator
     */
    public CodeGenerator addImport(String importPackage) {
        if (isValid(importPackage)) {
            appendNewLine(IMP);
            append(importPackage);
            endLine();
        }
        return this;
    }

    /**
     * This method defines class name and encapsulation modifier.
     * @param encapsulation Encapsulation (PUBLIC/PRIVATE/PACKAGE/PROTECTED)
     * @param name String (Class name)
     * @return CodeGenerator
     */
    public CodeGenerator defineClass(Encapsulation encapsulation, String name) {
        return defineClass(encapsulation, name, null);
    }

    /**
     * This method defines class name, parent class name and encapsulation modifier.
     * @param encapsulation Encapsulation (PUBLIC/PRIVATE/PACKAGE/PROTECTED)
     * @param name String (Class name)
     * @param extendedPart String (Parent class name)
     * @return CodeGenerator
     */
    public CodeGenerator defineClass(Encapsulation encapsulation, String name, String extendedPart) {
        append(LINE_BREAK);
        append(getEncapsulation(encapsulation));
        append("class ");
        append(name);
        if (isValid(extendedPart)) {
            append(SPACE);
            append(extendedPart);
            append(SPACE);
        }
        openBlock();
        return this;
    }

    /**
     * Add simple parameter with specified <i>encapsulation</i>, <i>type</i> and <i>name</i>
     * @param encapsulation Encapsulation (PUBLIC/PRIVATE/PACKAGE/PROTECTED)
     * @param type String
     * @param name String
     * @return CodeGenerator
     */
    public CodeGenerator addField(Encapsulation encapsulation, String type, String name) {
        fields.put(name, type);
        appendNewLine(getEncapsulation(encapsulation));
        append(type);
        append(SPACE);
        append(name);
        endLine();
        return this;
    }

    /**
     * Add simple parameter with specified <i>encapsulation</i>, <i>type</i> and <i>name</i>
     * and default value
     * @param encapsulation Encapsulation (PUBLIC/PRIVATE/PACKAGE/PROTECTED)
     * @param type String
     * @param name String
     * @param defaultValue String
     * @return CodeGenerator
     */
    public CodeGenerator addField(Encapsulation encapsulation, String type, String name, String defaultValue) {
        fields.put(name, type);
        appendNewLine(getEncapsulation(encapsulation));
        append(type);
        append(SPACE);
        append(name);
        append(" = ");
        append(defaultValue);
        endLine();
        return this;
    }

    /**
     * Add method.
     * @param method Method
     * @return CodeGenerator
     */
    public CodeGenerator addMethod(Method method) {
        method.build(closingsRequired, builder);
        return this;
    }

    /**
     * Add custom line to the code.
     * @param line String
     * @return CodeGenerator
     */
    public CodeGenerator addCustomLine(String line) {
        appendNewLine(line);
        append(LINE_BREAK);
        return this;
    }

    /**
     * Add custom block and open new block.<br>
     *     Ex., while(true) {
     * @param line String
     * @return CodeGenerator
     */
    public CodeGenerator addCustomCodeAndOpenBlock(String line) {
        appendNewLine(line);
        openBlock();
        return this;
    }



    // Helpers

    private void endLine() {
        builder.append(SEMI_COL).append(LINE_BREAK);
    }

    private void appendNewLine(String element) {
        builder.append(addTabsBefore(closingsRequired, element));
    }

    private void append(String element) {
        builder.append(element);
    }

    private void openBlock() {
        builder.append(BLOCK_OPEN)
                .append(LINE_BREAK);
        closingsRequired++;
    }

    public CodeGenerator closeBlock() {
        if (closingsRequired == 0) {
            return this;
        }
        builder.append(addTabsBefore(closingsRequired - 1, BLOCK_CLOSE));
        closingsRequired--;
        if (closingsRequired > 0) builder.append(LINE_BREAK);
        return this;
    }

    private void closeAllBlocks() {
        int loops = closingsRequired;
        for (int i = 0; i < loops; i++) {
            closeBlock();
        }
    }

    private String addTabBefore(String line) {
        return TAB + line;
    }

    private String addTabsBefore(int tabCount, String line) {
        StringBuilder tabs = new StringBuilder();
        for (int i = 0; i < tabCount; i++) {
            tabs.append(TAB);
        }
        return tabs.toString() + line;
    }

    private String getSpaces(int spaceCount) {
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < spaceCount; i++) {
            spaces.append(SPACE);
        }
        return spaces.toString();
    }

    static boolean isValid(String pieceOfCode) throws IllegalArgumentException {
        if (pieceOfCode == null) return false;
        if (pieceOfCode.isEmpty() || pieceOfCode.trim().contains(" ")) {
            throw new IllegalArgumentException("Literals cannot be empty or contain whitespace.");
        }
        return true;
    }

    public String build() {
        closeAllBlocks();
        return builder.toString();
    }
}
