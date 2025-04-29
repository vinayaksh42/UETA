package org.vinayak;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;

public class JavaCompilerUtil {
    public static boolean compileJavaFile(String javaFilePath, String classOutputFolder, String libraryJarPath) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new RuntimeException("No Java compiler available. Are you running inside a JDK?");
        }

        File outputDir = new File(classOutputFolder);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        System.out.println("Compiling Java File: " + javaFilePath);
        System.out.println("Compiling Output Dir: " + classOutputFolder);

        int result = compiler.run(
                null, null, null,
                "-classpath", libraryJarPath,
                "-d", classOutputFolder,
                javaFilePath);

        System.out.println("Compiler exited with code: " + result);
        return result == 0;
    }
}
