package org.vinayak;

import java.util.*;

import soot.jimple.infoflow.Infoflow;
import soot.jimple.infoflow.InfoflowConfiguration.ImplicitFlowMode;
import soot.jimple.infoflow.results.InfoflowResults;

public class FlowDroidDemo {
        public static InfoflowResults pathConditionFinder(String str) {

                // Example usage of flowDroid
                Infoflow infoflow = new Infoflow();
                infoflow.getConfig().setImplicitFlowMode(ImplicitFlowMode.AllImplicitFlows);
                infoflow.getConfig().setInspectSinks(false);
                List<String> sourceMethods = new ArrayList<>();
                sourceMethods.add(
                                "<FlowDroidExampleCode: java.lang.String source()>");

                List<String> sinkMethods = new ArrayList<>();
                sinkMethods.add(
                                "<java.lang.RuntimeException: void <init>(java.lang.String)>");

                infoflow.computeInfoflow(
                                "src/test/resources/bytecode", null,
                                str,
                                sourceMethods,
                                sinkMethods);// replace with ISourceSinkManager

                InfoflowResults results = infoflow.getResults();
                System.out.println("Results: " + results);
                return results;

        }
}
