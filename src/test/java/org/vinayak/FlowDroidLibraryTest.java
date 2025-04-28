package org.vinayak;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

import soot.jimple.infoflow.results.InfoflowResults;

public class FlowDroidLibraryTest {
    @Test
    public void addFuncToNullPointerException() {
        String methodSignature = "<DriverStub: void run()>";
        String JarPath = "resources/httpcore-4.4.16.jar";
        InfoflowResults results = Main.flowDroidExceptionAnalysis(methodSignature, JarPath);
        assertFalse(results.isEmpty());
    }
}
