package org.vinayak;

import java.util.*;
import java.util.regex.Pattern;
import java.io.IOException;
import java.util.regex.Matcher;

import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.options.Options;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.sourcesSinks.manager.ISourceSinkManager;
import soot.jimple.infoflow.android.source.parsers.xml.XMLSourceSinkParser;
import soot.jimple.infoflow.entryPointCreators.DefaultEntryPointCreator;
import soot.jimple.infoflow.sourcesSinks.definitions.ISourceSinkDefinition;
import soot.jimple.infoflow.sourcesSinks.manager.BaseSourceSinkManager;

public class Main {
  public static void main(String[] args) {
    String methodSignature = "<org.apache.http.HttpHost: void <init>(java.lang.String,int)>";
    String JarPath = "resources/httpcore-4.4.16.jar";
    java.util.List<String> sinks = Arrays.asList(
        "java.lang.IllegalArgumentException: void &lt;init&gt;(java.lang.String)",
        "java.lang.NullPointerException: void &lt;init&gt;(java.lang.String)");
    InfoflowResults results = Main.flowDroidExceptionAnalysis(methodSignature, JarPath, sinks);
    System.out.println("Results: " + results);
  }

  static class SimpleSourceSinkManager extends BaseSourceSinkManager {
    public SimpleSourceSinkManager(Collection<? extends ISourceSinkDefinition> sources,
        Collection<? extends ISourceSinkDefinition> sinks, InfoflowConfiguration config) {
      super(sources, sinks, config);
    }

    @Override
    protected boolean isEntryPointMethod(soot.SootMethod method) {
      return false;
    }
  }

  private static String[] parseParamTypes(String methodSignature) {
    Pattern p = Pattern.compile("<(.+?): (.+?) (.+?)\\((.*?)\\)>");
    Matcher m = p.matcher(methodSignature);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid method signature: " + methodSignature);
    }
    String params = m.group(4);
    return params.isEmpty() ? new String[0] : params.split(",");
  }

  public static InfoflowResults flowDroidExceptionAnalysis(String MethodSignature, String LibraryJarPath,
      List<String> sinkSignatures) {

    String driverJavaPath = "resources/DriverStub.java";
    String driverClassFolder = "resources";
    String xmlPath = "src/main/resources/SourcesAndSinks.xml";

    String[] paramTypes = parseParamTypes(MethodSignature);

    DriverStubGenerator.generateDriverStub(MethodSignature, driverJavaPath);
    System.out.println("Generated DriverStub.java");

    XMLGenerator.generateSourcesAndSinksXML(paramTypes, sinkSignatures, xmlPath);
    System.out.println("Generated SourcesAndSinks.xml");

    boolean compileSuccess = JavaCompilerUtil.compileJavaFile(driverJavaPath, driverClassFolder, LibraryJarPath);
    if (!compileSuccess) {
      throw new RuntimeException("Failed to compile DriverStub.java");
    }
    System.out.println("Compiled DriverStub.class");

    String javaHome = System.getProperty("java.home");
    String rtJarPath = javaHome + "/lib/rt.jar";
    String sootClassPath = String.join(
        System.getProperty("path.separator"),
        driverClassFolder,
        LibraryJarPath,
        rtJarPath);

    Infoflow infoflow = new Infoflow();
    infoflow.getConfig().setImplicitFlowMode(InfoflowConfiguration.ImplicitFlowMode.AllImplicitFlows);
    infoflow.getConfig().setInspectSinks(false);
    infoflow.getConfig().setOneSourceAtATime(false);
    infoflow.getConfig().setCallgraphAlgorithm(InfoflowConfiguration.CallgraphAlgorithm.RTA);
    infoflow.getConfig().setStaticFieldTrackingMode(InfoflowConfiguration.StaticFieldTrackingMode.ContextFlowSensitive);

    XMLSourceSinkParser parser;
    try {
      parser = XMLSourceSinkParser.fromFile(xmlPath);
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse XML file: " + xmlPath, e);
    }
    ISourceSinkManager ssm = new SimpleSourceSinkManager(parser.getSources(), parser.getSinks(), infoflow.getConfig());

    DefaultEntryPointCreator entryCreator = new DefaultEntryPointCreator(
        Collections.singletonList("<DriverStub: void run()>"));

    infoflow.setSootConfig(new IInfoflowConfig() {
      @Override
      public void setSootOptions(Options options, InfoflowConfiguration config) {
        options.set_prepend_classpath(true);
        options.set_soot_classpath(sootClassPath);
        options.set_allow_phantom_refs(true);
      }
    });

    infoflow.computeInfoflow(
        driverClassFolder,
        null,
        entryCreator,
        ssm);

    InfoflowResults results = infoflow.getResults();
    System.out.println("Results: " + results);
    return results;
  }
}
