/*
 * Copyright (c) 2002 - 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 */
package com.ibm.wala.cast.js.callgraph.fieldbased.flowgraph.vertices;

import com.ibm.wala.cast.js.ipa.summaries.JavaScriptConstructorFunctions.JavaScriptConstructor;
import com.ibm.wala.cast.js.types.JavaScriptMethods;
import com.ibm.wala.cast.js.types.JavaScriptTypes;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.types.AstMethodReference;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.collections.EmptyIterator;
import com.ibm.wala.util.collections.NonNullSingletonIterator;
import com.ibm.wala.util.collections.Pair;
import java.util.Iterator;
import java.util.Set;

/**
 * A function vertex represents a function object (or, more precisely, all function objects arising
 * from a single function expression or declaration).
 *
 * @author mschaefer
 */
public class FuncVertex extends Vertex implements ObjectVertex {
  // the IClass representing this function in the class hierarchy
  protected final IClass klass;
  //Context identifier for COP
  private final String context;

  /**
  FuncVertex(IClass method) {
    this.klass = method;
  }
  */
  
  public FuncVertex(IClass method, String context) {
	    this.klass = method;
	    this.context = context != null ? context : "default"; // Default context if none provided
	}


  public String getFullName() {
    return klass.getName().toString();
  }
  
  public String getContext() {
	    return context;
  }


  @Override
  public <T> T accept(VertexVisitor<T> visitor) {
    return visitor.visitFuncVertex(this);
  }
  /**
  @Override
  public String toString() {
    String methodName = klass.getName().toString();
    return "Func(" + methodName.substring(methodName.lastIndexOf('/') + 1) + ')';
  }
  */
  
  @Override
  public String toString() {
      String methodName = klass.getName().toString();
      return "Func(" + methodName.substring(methodName.lastIndexOf('/') + 1) + ", Context: " + context + ")";
  }
  
  // Contain contextual variants
  public FuncVertex withContext(String newContext) {
	    return new FuncVertex(this.klass, newContext);
	}

  /**
  @Override
  public Iterator<Pair<CGNode, NewSiteReference>> getCreationSites(CallGraph CG) {
    MethodReference ctorRef = JavaScriptMethods.makeCtorReference(JavaScriptTypes.Function);
    Set<CGNode> f = CG.getNodes(ctorRef);
    CGNode ctor = null;
    for (CGNode n : f) {
      JavaScriptConstructor c = (JavaScriptConstructor) n.getMethod();
      if (c.constructedType().equals(klass)) {
        ctor = n;
        break;
      }
    }

    // built in objects
    if (ctor == null) {
      return EmptyIterator.instance();
    }

    Iterator<CGNode> callers = CG.getPredNodes(ctor);
    CGNode caller = callers.next();
    assert !callers.hasNext();

    Iterator<CallSiteReference> sites = CG.getPossibleSites(caller, ctor);
    CallSiteReference site = sites.next();
    assert !sites.hasNext()
        : caller + " --> " + ctor + " @ " + site + " and " + sites.next() + '\n' + caller.getIR();

    return NonNullSingletonIterator.make(
        Pair.make(caller, NewSiteReference.make(site.getProgramCounter(), klass.getReference())));
  }
  */
  
  @Override
  public Iterator<Pair<CGNode, NewSiteReference>> getCreationSites(CallGraph CG) {
      MethodReference ctorRef = JavaScriptMethods.makeCtorReference(JavaScriptTypes.Function);
      Set<CGNode> f = CG.getNodes(ctorRef);
      CGNode ctor = null;
      
      for (CGNode n : f) {
          JavaScriptConstructor c = (JavaScriptConstructor) n.getMethod();
          if (c.constructedType().equals(klass)) {
              ctor = n;
              break;
          }
      }

      if (ctor == null) {
          return EmptyIterator.instance();
      }

      Iterator<CGNode> callers = CG.getPredNodes(ctor);
      CGNode caller = callers.next();
      assert !callers.hasNext();

      Iterator<CallSiteReference> sites = CG.getPossibleSites(caller, ctor);
      CallSiteReference site = sites.next();
      assert !sites.hasNext()
          : caller + " --> " + ctor + " @ " + site + " and " + sites.next() + '\n' + caller.getIR();

      System.out.println("Returning creation sites for context: " + context);
      return NonNullSingletonIterator.make(
          Pair.make(caller, NewSiteReference.make(site.getProgramCounter(), klass.getReference())));
  }


  @Override
  public IClass getConcreteType() {
    return klass;
  }
  /**
  @Override
  public String toSourceLevelString(IAnalysisCacheView cache) {
    AstMethod method = (AstMethod) klass.getMethod(AstMethodReference.fnSelector);
    if (method != null) {
      return "Func(" + method.getSourcePosition().prettyPrint() + ")";
    } else {
      return toString();
    }
  }
  */
  
  @Override
  public String toSourceLevelString(IAnalysisCacheView cache) {
      AstMethod method = (AstMethod) klass.getMethod(AstMethodReference.fnSelector);
      if (method != null) {
          return "Func(" + method.getSourcePosition().prettyPrint() + ", Context: " + context + ")";
      } else {
          return toString();
      }
  }

}
