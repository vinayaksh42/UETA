package org.vinayak;

import java.util.*;
import java.io.IOException;

import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.options.Options;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.InfoflowConfiguration.ImplicitFlowMode;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.sourcesSinks.manager.ISourceSinkManager;
import soot.jimple.infoflow.android.source.parsers.xml.XMLSourceSinkParser;
import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.jimple.infoflow.entryPointCreators.DefaultEntryPointCreator;
import soot.jimple.infoflow.sourcesSinks.definitions.ISourceSinkDefinition;
import soot.jimple.infoflow.sourcesSinks.manager.BaseSourceSinkManager;

public class Main {
  public static void main(String[] args) {
    String methodSignature = "<org.apache.http.HttpHost: void <init>(java.lang.String,int)>";
    String JarPath = "resources/httpcore-4.4.16.jar";
    InfoflowResults results = Main.flowDroidExceptionAnalysis(methodSignature, JarPath);
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

  public static InfoflowResults flowDroidExceptionAnalysis(String MethodSignature, String LibraryJarPath) {

    String driverClassFolder = "resources"; // where DriverStub.class is located

    String javaHome = System.getProperty("java.home");
    String rtJarPath = javaHome + "/lib/rt.jar";

    // Build soot classpath: driver folder + target library + rt.jar
    String sootClassPath = String.join(
        System.getProperty("path.separator"),
        driverClassFolder, // resources/ (for DriverStub.class)
        LibraryJarPath, // resources/httpcore-4.4.16.jar
        rtJarPath // standard Java classes
    );

    Infoflow infoflow = new Infoflow();
    infoflow.getConfig().setImplicitFlowMode(ImplicitFlowMode.AllImplicitFlows);
    infoflow.getConfig().setInspectSinks(false);
    infoflow.getConfig().setOneSourceAtATime(false);
    infoflow.getConfig().setCallgraphAlgorithm(InfoflowConfiguration.CallgraphAlgorithm.RTA);
    infoflow.getConfig().setStaticFieldTrackingMode(InfoflowConfiguration.StaticFieldTrackingMode.ContextFlowSensitive);

    XMLSourceSinkParser parser = null;
    try {
      parser = XMLSourceSinkParser.fromFile("src/main/resources/SourcesAndSinks.xml");
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("Error parsing XML file");
    }

    ISourceSinkManager ssm = new SimpleSourceSinkManager(
        parser.getSources(),
        parser.getSinks(),
        infoflow.getConfig());

    DefaultEntryPointCreator entryCreator = new DefaultEntryPointCreator(
        Collections.singletonList(MethodSignature));

    infoflow.setSootConfig(new IInfoflowConfig() {
      @Override
      public void setSootOptions(Options options, InfoflowConfiguration config) {
        options.set_prepend_classpath(true);
        options.set_soot_classpath(sootClassPath);
        options.set_allow_phantom_refs(true);
      }
    });

    // Analyze the folder containing DriverStub.class
    infoflow.computeInfoflow(
        driverClassFolder, // <-- NOT the httpcore jar
        null,
        entryCreator,
        ssm);

    InfoflowResults results = infoflow.getResults();
    System.out.println("Results: " + results);
    return results;
  }
}
