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

import com.ibm.wala.cast.js.ssa.JavaScriptInvoke;
import com.ibm.wala.cast.js.types.JavaScriptMethods;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.types.AstMethodReference;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;

/**
 * A call vertex represents the possible callees of a function call or {@code new} expression.
 *
 * @author mschaefer
 */
public class CallVertex extends Vertex {
  // method containing the call
  private final FuncVertex func;

  // PC of the call site
  private final CallSiteReference site;

  // the call instruction itself
  private final JavaScriptInvoke invk;
  
  //Field to store the context in which this call occurs
  private final String context;

  /**
  CallVertex(FuncVertex func, CallSiteReference site, JavaScriptInvoke invk) {
    this.func = func;
    this.site = site;
    this.invk = invk;
  }
  */
  CallVertex(FuncVertex func, CallSiteReference site, JavaScriptInvoke invk) {
	    this(func, site, invk, "default");
	}

	/**
	 * Constructor to create a call vertex with an associated context.
	 */
	CallVertex(FuncVertex func, CallSiteReference site, JavaScriptInvoke invk, String context) {
	    this.func = func;
	    this.site = site;
	    this.invk = invk;
	    this.context = context;
	}


  public FuncVertex getCaller() {
    return func;
  }

  public CallSiteReference getSite() {
    return site;
  }

  public JavaScriptInvoke getInstruction() {
    return invk;
  }
  
  /**
   * Retrieves the context associated with this call.
   */
  public String getContext() {
      return context;
  }

  /**
   * Returns a new CallVertex with a different context.
   */
  public CallVertex withContext(String newContext) {
      return new CallVertex(func, site, invk, newContext);
  }


  /** Does this call vertex correspond to a {@code new} instruction? */
  public boolean isNew() {
    return site.getDeclaredTarget() == JavaScriptMethods.ctorReference;
  }

  @Override
  public <T> T accept(VertexVisitor<T> visitor) {
    return visitor.visitCalleeVertex(this);
  }
  
  /**
  @Override
  public String toString() {
    return "Callee(" + func + ", " + site + ')';
  }
  */
  
  @Override
  public String toString() {
      return "Callee(" + func + ", " + site + ", context=" + context + ')';
  }

  /**
  @Override
  public String toSourceLevelString(IAnalysisCacheView cache) {
    IClass concreteType = func.getConcreteType();
    AstMethod method = (AstMethod) concreteType.getMethod(AstMethodReference.fnSelector);
    return "Callee(" + method.getSourcePosition(site.getProgramCounter()).prettyPrint() + ")";
  }
  */
  @Override
  public String toSourceLevelString(IAnalysisCacheView cache) {
      IClass concreteType = func.getConcreteType();
      AstMethod method = (AstMethod) concreteType.getMethod(AstMethodReference.fnSelector);
      return "Callee(" + method.getSourcePosition(site.getProgramCounter()).prettyPrint() +
             ", context=" + context + ")";
  }

  
  @Override
  public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof CallVertex)) return false;
      CallVertex other = (CallVertex) obj;
      return func.equals(other.func) && 
             site.equals(other.site) && 
             invk.equals(other.invk) &&
             context.equals(other.context);
  }

  @Override
  public int hashCode() {
      int result = func.hashCode();
      result = 31 * result + site.hashCode();
      result = 31 * result + invk.hashCode();
      result = 31 * result + context.hashCode();
      return result;
  }

}
