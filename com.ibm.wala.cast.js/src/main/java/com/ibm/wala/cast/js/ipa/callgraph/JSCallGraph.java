/*
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 */
package com.ibm.wala.cast.js.ipa.callgraph;

import com.ibm.wala.cast.ipa.callgraph.AstCallGraph;
import com.ibm.wala.cast.js.cfg.JSInducedCFG;
import com.ibm.wala.cast.js.ssa.JavaScriptInvoke;
import com.ibm.wala.cast.js.types.JavaScriptMethods;
import com.ibm.wala.cast.js.types.JavaScriptTypes;
import com.ibm.wala.cast.loader.DynamicCallSiteReference;
import com.ibm.wala.cfg.InducedCFG;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.impl.FakeRootMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.collections.HashSetFactory;
import java.util.Set;

public class JSCallGraph extends AstCallGraph {

  public JSCallGraph(IMethod fakeRootClass, AnalysisOptions options, IAnalysisCacheView cache) {
    super(fakeRootClass, options, cache);
  }

  public static final MethodReference fakeRoot =
      MethodReference.findOrCreate(
          JavaScriptTypes.FakeRoot, FakeRootMethod.name, FakeRootMethod.descr);

  public static class JSFakeRoot extends ScriptFakeRoot {

    public JSFakeRoot(IClassHierarchy cha, AnalysisOptions options, IAnalysisCacheView cache) {
      super(fakeRoot, cha.lookupClass(JavaScriptTypes.FakeRoot), cha, options, cache);
    }

    @Override
    public InducedCFG makeControlFlowGraph(SSAInstruction[] instructions) {
      return new JSInducedCFG(instructions, this, Everywhere.EVERYWHERE);
    }
    /**
    @Override
    public SSAAbstractInvokeInstruction addDirectCall(
        int function, int[] params, CallSiteReference site) {
      CallSiteReference newSite =
          new DynamicCallSiteReference(JavaScriptTypes.CodeBody, statements.size());

      JavaScriptInvoke s =
          new JavaScriptInvoke(
              statements.size(), function, nextLocal++, params, nextLocal++, newSite);
      statements.add(s);

      return s;
    }
    **/
    
    @Override
    public SSAAbstractInvokeInstruction addDirectCall(
        int function, int[] params, CallSiteReference site) {

      CallSiteReference newSite =
          new DynamicCallSiteReference(JavaScriptTypes.CodeBody, statements.size());

      JavaScriptInvoke s =
          new JavaScriptInvoke(
              statements.size(), function, nextLocal++, params, nextLocal++, newSite);

      statements.add(s);

      // Additional COP handling logic
      IMethod targetMethod = cha.resolveMethod(site.getDeclaredTarget());
      if (targetMethod != null) {
          String functionName = targetMethod.getName().toString();
          if (functionName.equals("adapt")) {
              handleContextAdaptation(s, params);
          } else if (functionName.equals("activate")) {
              handleContextActivation(s, params);
          }
      }

      return s;
    }
    
    /**
     * Handle Context.adapt(object, trait) by updating the call graph.
     * Adds flow edges to track the adaptation relationship.
     */
    private void handleContextAdaptation(JavaScriptInvoke invoke, int[] params) {
        System.out.println("Handling context adaptation at site: " + invoke.getCallSite());

        // Extract the object and trait being adapted from the instruction parameters
        int objectVar = params[1];  // The object being adapted
        int traitVar = params[2];   // The trait being applied

        // Add flow edges to track adaptation relationships in the call graph
        CGNode caller = findOrCreateNode(this, Everywhere.EVERYWHERE);
        CallSiteReference site = invoke.getCallSite();

        CGNode objectNode = findOrCreateNode(caller.getMethod(), Everywhere.EVERYWHERE);
        CGNode traitNode = findOrCreateNode(caller.getMethod(), Everywhere.EVERYWHERE);

        addEdge(caller, objectNode);
        addEdge(objectNode, traitNode);

        System.out.println("Context.adapt() linking object variable " + objectVar 
                            + " to trait variable " + traitVar);
    }
    
    /**
     * Handle Context.activate(context) by adding flow edges.
     * Ensures proper propagation of context activation effects.
     */
    private void handleContextActivation(JavaScriptInvoke invoke, int[] params) {
        System.out.println("Handling context activation at site: " + invoke.getCallSite());

        // Extract the context variable from the instruction parameters
        int contextVar = params[1];

        // Find or create call graph nodes to track the context activation
        CGNode caller = findOrCreateNode(this, Everywhere.EVERYWHERE);
        CallSiteReference site = invoke.getCallSite();

        CGNode contextNode = findOrCreateNode(caller.getMethod(), Everywhere.EVERYWHERE);
        CGNode globalNode = findOrCreateNode(getFakeRootNode().getMethod(), Everywhere.EVERYWHERE);

        // Add edges to propagate the context activation effect
        addEdge(caller, contextNode);
        addEdge(contextNode, globalNode);

        System.out.println("Context.activate() activated for context variable " + contextVar);
    }

    /**
     * Find or create a call graph node for a given method.
     */
    private CGNode findOrCreateNode(IMethod method, Context context) {
        return this.getCallGraph().findOrCreateNode(method, context);
    }

    /**
     * Add an edge between two nodes in the call graph.
     */
    private void addEdge(CGNode from, CGNode to) {
        if (!this.getCallGraph().hasEdge(from, to)) {
            this.getCallGraph().addEdge(from, to);
        }
    }

  }

  @Override
  protected CGNode makeFakeWorldClinitNode() {
    return null;
  }

  @Override
  protected CGNode makeFakeRootNode() throws com.ibm.wala.util.CancelException {
    return findOrCreateNode(
        new JSFakeRoot(cha, options, getAnalysisCache()), Everywhere.EVERYWHERE);
  }

  @Override
  public Set<CGNode> getNodes(MethodReference m) {
    if (m.getName().equals(JavaScriptMethods.ctorAtom)) {
      // TODO cache this?
      Set<CGNode> result = HashSetFactory.make(1);
      for (CGNode n : this) {
        IMethod method = n.getMethod();
        if (method.getName().equals(JavaScriptMethods.ctorAtom)
            && method.getDeclaringClass().getReference().equals(m.getDeclaringClass())) {
          result.add(n);
        }
      }
      return result;
    } else {
      return super.getNodes(m);
    }
  }
}
