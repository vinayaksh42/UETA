package org.vinayak;

import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DriverStubGenerator {

    public static void generateDriverStub(String methodSignature, String outputFilePath) {
        Pattern p = Pattern.compile("<(.+?): (.+?) (.+?)\\((.*?)\\)>");
        Matcher m = p.matcher(methodSignature);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid method signature: " + methodSignature);
        }

        String className = m.group(1);
        String returnType = m.group(2);
        String methodName = m.group(3);
        String params = m.group(4);

        String[] paramTypes = params.isEmpty() ? new String[0] : params.split(",");

        StringBuilder sb = new StringBuilder();
        sb.append("public class DriverStub {\n\n");

        for (int i = 0; i < paramTypes.length; i++) {
            sb.append("    public static ").append(mapType(paramTypes[i])).append(" source").append(i).append("() {\n");
            sb.append("        return ").append(dummyReturn(paramTypes[i])).append(";\n");
            sb.append("    }\n\n");
        }

        sb.append("    public static void run() {\n");
        if ("<init>".equals(methodName)) {
            sb.append("        new ").append(className).append("(");
        } else {
            sb.append("        new ").append(className).append("().").append(methodName).append("(");
        }

        for (int i = 0; i < paramTypes.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append("source").append(i).append("()");
        }
        sb.append(");\n");

        sb.append("    }\n");
        sb.append("}\n");

        try (FileWriter writer = new FileWriter(outputFilePath)) {
            writer.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error writing DriverStub.java");
        }
    }

    private static String mapType(String type) {
        switch (type) {
            case "int":
            case "long":
            case "float":
            case "double":
            case "boolean":
            case "byte":
            case "short":
            case "char":
                return type;
            default:
                return type;
        }
    }

    private static String dummyReturn(String type) {
        switch (type) {
            case "int":
            case "short":
            case "byte":
            case "long":
                return "0";
            case "float":
                return "0.0f";
            case "double":
                return "0.0";
            case "boolean":
                return "false";
            case "char":
                return "'a'";
            default:
                return "null";
        }
    }
}
