package com.ibm.wala.ssa;

/**
 * SSAInstruction subclass for handling Context-Oriented Programming (COP)
 * Adaptation and Activation instructions.
 *
 * These instructions model the dynamic adaptation of objects with traits
 * and their activation in different contexts.
 */
public class SSAContextAdaptInstruction extends SSAInstruction {
    private final int objectVar;
    private final int traitVar;

    public SSAContextAdaptInstruction(int iindex, int objectVar, int traitVar) {
        super(iindex);
        this.objectVar = objectVar;
        this.traitVar = traitVar;
    }

    public int getObjectVar() {
        return objectVar;
    }

    public int getTraitVar() {
        return traitVar;
    }

    @Override
    public void visit(IVisitor v) {
        if (v instanceof SSAInstruction.IVisitor) {
            ((SSAInstruction.IVisitor) v).visitContextAdapt(this);
        }
    }

    @Override
    public SSAInstruction copyForSSA(SSAInstructionFactory insts, int[] defs, int[] uses) {
        return new SSAContextAdaptInstruction(iIndex(), uses[0], uses[1]);
    }

    @Override
    public boolean isFallThrough() {
        return true;
    }

    @Override
    public String toString() {
        return "ContextAdapt(iIndex=" + iIndex() + ", object=" + objectVar + ", trait=" + traitVar + ")";
    }
}

public class SSAContextActivateInstruction extends SSAInstruction {
    private final int contextVar;

    public SSAContextActivateInstruction(int iindex, int contextVar) {
        super(iindex);
        this.contextVar = contextVar;
    }

    public int getContextVar() {
        return contextVar;
    }

    @Override
    public void visit(IVisitor v) {
        if (v instanceof SSAInstruction.IVisitor) {
            ((SSAInstruction.IVisitor) v).visitContextActivate(this);
        }
    }

    @Override
    public SSAInstruction copyForSSA(SSAInstructionFactory insts, int[] defs, int[] uses) {
        return new SSAContextActivateInstruction(iIndex(), uses[0]);
    }

    @Override
    public boolean isFallThrough() {
        return true;
    }

    @Override
    public String toString() {
        return "ContextActivate(iIndex=" + iIndex() + ", context=" + contextVar + ")";
    }
}
