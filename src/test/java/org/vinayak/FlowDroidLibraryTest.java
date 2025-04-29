package org.vinayak;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;

import soot.jimple.infoflow.results.InfoflowResults;

public class FlowDroidLibraryTest {
    @Test
    public void testHttpCore446() {
        String methodSignature = "<org.apache.http.HttpHost: void <init>(java.lang.String,int)>";
        String JarPath = "resources/httpcore-4.4.6.jar";
        java.util.List<String> sinks = Arrays.asList(
                "java.lang.IllegalArgumentException: void &lt;init&gt;(java.lang.String)");
        InfoflowResults results = Main.flowDroidExceptionAnalysis(methodSignature, JarPath, sinks);
        assertEquals(2, results.getResults().size());

    }

    @Test
    public void testHttpCore4416() {
        String methodSignature = "<org.apache.httpcore.HttpHost: void <init>(java.lang.String,int)>";
        String JarPath = "resources/httpcore-4.4.16.jar";
        java.util.List<String> sinks = Arrays.asList(
                "java.lang.IllegalArgumentException: void &lt;init&gt;(java.lang.String)");
        InfoflowResults results = Main.flowDroidExceptionAnalysis(methodSignature, JarPath, sinks);
        assertEquals(3, results.getResults().size());
    }
}
