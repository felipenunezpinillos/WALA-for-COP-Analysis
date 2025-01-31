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

import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.types.AstMethodReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.util.debug.Assertions;
import java.util.Arrays;

/**
 * A variable vertex represents an SSA variable inside a given function.
 *
 * @author mschaefer
 */
public final class VarVertex extends Vertex implements PointerKey {
  private final FuncVertex func;
  private final int valueNumber;
  //Field to store the context in which this variable is used
  private final String context;

 /**
  VarVertex(FuncVertex func, int valueNumber) {
    Assertions.productionAssertion(valueNumber >= 0, "Invalid value number for VarVertex");
    this.func = func;
    this.valueNumber = valueNumber;
  }
  */
  
  VarVertex(FuncVertex func, int valueNumber) {
	    this(func, valueNumber, "default");
	}

	/**
	 * Constructor to create a variable vertex with an associated context.
	 */
	VarVertex(FuncVertex func, int valueNumber, String context) {
	    Assertions.productionAssertion(valueNumber >= 0, "Invalid value number for VarVertex");
	    this.func = func;
	    this.valueNumber = valueNumber;
	    this.context = context;
	}


  public FuncVertex getFunction() {
    return func;
  }

  public int getValueNumber() {
    return valueNumber;
  }
  
  /**
   * Retrieves the context associated with this variable.
   */
  public String getContext() {
      return context;
  }

  /**
   * Returns a new VarVertex in a different context.
   */
  public VarVertex withContext(String newContext) {
      return new VarVertex(func, valueNumber, newContext);
  }


  @Override
  public <T> T accept(VertexVisitor<T> visitor) {
    return visitor.visitVarVertex(this);
  }

  /**
  @Override
  public String toString() {
    return "Var(" + func + ", " + valueNumber + ')';
  }
  */
  
  @Override
  public String toString() {
      return "Var(" + func + ", " + valueNumber + ", context=" + context + ')';
  }

  /**
  @Override
  public String toSourceLevelString(IAnalysisCacheView cache) {
    // we want to get a variable name rather than a value number
    IClass concreteType = func.getConcreteType();
    AstMethod method = (AstMethod) concreteType.getMethod(AstMethodReference.fnSelector);
    IR ir = cache.getIR(method);
    String methodPos = method.getSourcePosition().prettyPrint();
    // we rely on the fact that the CAst IR ignores the index position!
    String[] localNames = ir.getLocalNames(0, valueNumber);
    StringBuilder result = new StringBuilder("Var(").append(methodPos).append(", ");
    if (localNames != null && localNames.length > 0) {
      result.append(Arrays.toString(localNames));
    } else {
      result.append("%ssa_val ").append(valueNumber);
    }
    result.append(")");
    return result.toString();
  }
  */
  
  @Override
  public String toSourceLevelString(IAnalysisCacheView cache) {
      IClass concreteType = func.getConcreteType();
      AstMethod method = (AstMethod) concreteType.getMethod(AstMethodReference.fnSelector);
      IR ir = cache.getIR(method);
      String methodPos = method.getSourcePosition().prettyPrint();
      String[] localNames = ir.getLocalNames(0, valueNumber);

      StringBuilder result = new StringBuilder("Var(")
              .append(methodPos)
              .append(", ");

      if (localNames != null && localNames.length > 0) {
          result.append(Arrays.toString(localNames));
      } else {
          result.append("%ssa_val ").append(valueNumber);
      }
      result.append(", context=").append(context);
      result.append(")");

      return result.toString();
  }
  
  @Override
  public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof VarVertex)) return false;
      VarVertex other = (VarVertex) obj;
      return valueNumber == other.valueNumber && func.equals(other.func) && context.equals(other.context);
  }

  @Override
  public int hashCode() {
      return 31 * func.hashCode() + valueNumber + context.hashCode();
  }


}
