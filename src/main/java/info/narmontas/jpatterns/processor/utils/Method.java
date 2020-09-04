package info.narmontas.jpatterns.processor.utils;

import java.util.*;

/**
 * <p>Method class provides tools for method generation.</p>
 * <p>Result of the tools is Method instance that can be
 * added to CodeGenerator instance (CodeGenerator.addMethod(Method)).</p>
 */
public class Method {
    private final String methodName;
    private Encapsulation encapsulation = Encapsulation.NONE;
    private int localIndent = 1;
    private String returnType;
    private final HashMap<String, String> parameters = new LinkedHashMap<>();
    private final List<String> bodyLines = new ArrayList<>();
    private boolean isStatic = false;

    public Method(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Set method encapsulation
     * @param encapsulation Encapsulation (PUBLIC/PRIVATE/PACKAGE/PROTECTED)
     * @return Method
     */
    public Method setEncapsulation(Encapsulation encapsulation) {
        this.encapsulation = encapsulation;
        return this;
    }

    /**
     * Set method as static
     * @return Method
     */
    public Method setStatic() {
        this.isStatic = true;
        return this;
    }

    /**
     * Set method return type
     * @param returnType String
     * @return Method
     */
    public Method setReturnType(String returnType) {
        this.returnType = returnType;
        return this;
    }

    /**
     * Add method parameter
     * @param type String
     * @param name String
     * @return Method
     */
    public Method addParameter(String type, String name) {
        parameters.put(name, type);
        return this;
    }

    /**
     * Add simple body line
     * @param line String
     * @return Method
     */
    public Method addBodyLine(String line) {
        bodyLines.add(addTabsBefore(localIndent,  line + CodeGenerator.LINE_BREAK));
        return this;
    }

    /**
     * Add simple body line and start new block
     * @param line String
     * @return Method
     */
    public Method addBodyLineAndOpenBlock(String line) {
        bodyLines.add(addTabsBefore(localIndent,
                line + CodeGenerator.BLOCK_OPEN + CodeGenerator.LINE_BREAK));
        localIndent++;
        return this;
    }

    /**
     * Open new block
     * @return Method
     */
    public Method openBlock() {
        bodyLines.add(CodeGenerator.BLOCK_OPEN + CodeGenerator.LINE_BREAK);
        localIndent++;
        return this;
    }

    /**
     * Close current block
     * @return Method
     */
    public Method closeBlock() {
        if (localIndent == 0)
            return this;
        localIndent--;
        bodyLines.add(addTabsBefore(localIndent,
                CodeGenerator.BLOCK_CLOSE + CodeGenerator.LINE_BREAK));
        return this;
    }

    private void closeAllBlocks() {
        int loops = localIndent;
        for (int i = 0; i < loops; i++) {
            closeBlock();
        }
    }

    // fill builder lines
    void build(int closings, StringBuilder builder) {
        StringBuilder externalIndents = new StringBuilder();
        closeAllBlocks();
        for (int i = 0; i < closings; i++) {
            externalIndents.append(CodeGenerator.TAB);
        }
        String indent = externalIndents.toString();
        builder.append(CodeGenerator.LINE_BREAK);
        builder.append(indent);
        builder.append(CodeGenerator.getEncapsulation(encapsulation));
        if (isStatic) {
            builder.append(" static ");
        }
        if (CodeGenerator.isValid(returnType)) {
            builder.append(returnType);
        } else {
            builder.append("void");
        }
        builder.append(CodeGenerator.SPACE);
        builder.append(methodName);
        builder.append("(");
        boolean isTheFirstParam = true;
        for (Map.Entry<String, String> entry: parameters.entrySet()) {
            if (!isTheFirstParam) {
                builder.append(", ");
            }
            builder.append(entry.getValue());
            builder.append(CodeGenerator.SPACE);
            builder.append(entry.getKey());
            isTheFirstParam = false;

        }
        builder.append(")");
        builder.append(CodeGenerator.BLOCK_OPEN);
        builder.append(CodeGenerator.LINE_BREAK);
        for (String line: bodyLines) {
            builder.append(indent).append(line);
        }
    }

    private String addTabsBefore(int tabCount, String line) {
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < tabCount; i++) {
            indent.append(CodeGenerator.TAB);
        }
        return indent.toString() + line;
    }
}
