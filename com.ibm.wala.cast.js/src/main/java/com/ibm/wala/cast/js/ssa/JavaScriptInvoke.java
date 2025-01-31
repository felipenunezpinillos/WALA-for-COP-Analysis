package com.ibm.wala.cast.js.ssa;

import com.ibm.wala.cast.ir.ssa.MultiReturnValueInvokeInstruction;
import com.ibm.wala.cast.js.types.JavaScriptMethods;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInstructionFactory;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.TypeReference;
import java.util.Collection;

public class JavaScriptInvoke extends MultiReturnValueInvokeInstruction {
  /** The value numbers of the arguments passed to the call. */
  private final int[] params;
  private final int function;

  /** Context-related field for COP support */
  private final String context;

  // Existing constructors with added context support
  public JavaScriptInvoke(
      int iindex, int function, int results[], int[] params, int exception, CallSiteReference site, String context) {
    super(iindex, results, exception, site);
    this.function = function;
    this.params = params;
    this.context = context != null ? context : "default";
  }

  public JavaScriptInvoke(
      int iindex, int function, int result, int[] params, int exception, CallSiteReference site, String context) {
    this(iindex, function, new int[] {result}, params, exception, site, context);
  }

  public JavaScriptInvoke(
      int iindex, int function, int[] params, int exception, CallSiteReference site, String context) {
    this(iindex, function, null, params, exception, site, context);
  }

  // Constructor without context (backward compatibility)
  public JavaScriptInvoke(
      int iindex, int function, int[] params, int exception, CallSiteReference site) {
    this(iindex, function, params, exception, site, "default");
  }

  @Override
  public SSAInstruction copyForSSA(SSAInstructionFactory insts, int[] defs, int[] uses) {
    int fn = function;
    int newParams[] = params;

    if (uses != null) {
      int i = 0;
      fn = uses[i++];
      newParams = new int[params.length];
      for (int j = 0; j < newParams.length; j++) newParams[j] = uses[i++];
    }

    int[] newLvals = results.clone();
    int newExp = exception;

    if (defs != null) {
      int i = 0;
      if (getNumberOfReturnValues() > 0) {
        newLvals[0] = defs[i++];
      }
      newExp = defs[i++];
      for (int j = 1; j < getNumberOfReturnValues(); j++) {
        newLvals[j] = defs[i++];
      }
    }

    return ((JSInstructionFactory) insts).Invoke(iIndex(), fn, newLvals, newParams, newExp, site, context);
  }

  @Override
  public int getNumberOfUses() {
    return getNumberOfPositionalParameters();
  }

  @Override
  public String toString(SymbolTable symbolTable) {
    StringBuilder s = new StringBuilder();
    if (getNumberOfReturnValues() > 0) {
      s.append(getValueString(symbolTable, getReturnValue(0))).append(" = ");
    }
    if (site.getDeclaredTarget().equals(JavaScriptMethods.ctorReference)) s.append("construct ");
    else if (site.getDeclaredTarget().equals(JavaScriptMethods.dispatchReference)) s.append("dispatch ");
    else s.append("invoke ");
    s.append(getValueString(symbolTable, function));

    if (site != null) s.append('@').append(site.getProgramCounter());

    if (params != null) {
      if (params.length > 0) {
        s.append(' ').append(getValueString(symbolTable, params[0]));
      }
      for (int i = 1; i < params.length; i++) {
        s.append(',').append(getValueString(symbolTable, params[i]));
      }
    }

    s.append(" [context: ").append(context).append("]");

    if (exception == -1) {
      s.append(" exception: NOT MODELED");
    } else {
      s.append(" exception:").append(getValueString(symbolTable, exception));
    }

    return s.toString();
  }

  @Override
  public void visit(IVisitor v) {
    assert v instanceof JSInstructionVisitor;
    ((JSInstructionVisitor) v).visitJavaScriptInvoke(this);
  }

  @Override
  public int getNumberOfPositionalParameters() {
    if (params == null) {
      return 1;
    } else {
      return params.length + 1;
    }
  }

  @Override
  public int getUse(int j) {
    if (j == 0) return function;
    else if (j <= params.length) return params[j - 1];
    else {
      return super.getUse(j);
    }
  }

  public int getFunction() {
    return function;
  }

  public String getContext() {
    return context;
  }

  @Override
  public Collection<TypeReference> getExceptionTypes() {
    return Util.typeErrorExceptions();
  }

  @Override
  public int hashCode() {
    return site.hashCode() * function * 7529 + (context != null ? context.hashCode() : 0);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof JavaScriptInvoke)) return false;
    JavaScriptInvoke other = (JavaScriptInvoke) obj;
    return function == other.function &&
           site.equals(other.site) &&
           context.equals(other.context) &&
           paramsEqual(other.params);
  }

  private boolean paramsEqual(int[] otherParams) {
    if (params.length != otherParams.length) return false;
    for (int i = 0; i < params.length; i++) {
      if (params[i] != otherParams[i]) return false;
    }
    return true;
  }
}
