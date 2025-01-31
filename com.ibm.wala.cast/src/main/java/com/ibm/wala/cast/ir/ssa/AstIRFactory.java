package com.ibm.wala.cast.ir.ssa;

import com.ibm.wala.cast.ir.ssa.SSAConversion.SSAInformation;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.loader.AstMethod.LexicalInformation;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.cfg.AbstractCFG;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.cfg.IBasicBlock;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.TypeReference;

import java.util.Map;
import java.util.Map.Entry;

public class AstIRFactory<T extends IMethod> implements IRFactory<T> {

    private static final boolean enableCOPAnalysis = Boolean.getBoolean("enableCOPAnalysis");

    public ControlFlowGraph<?, ?> makeCFG(final IMethod method) {
        return ((AstMethod) method).getControlFlowGraph();
    }

    public static class AstDefaultIRFactory<T extends IMethod> extends DefaultIRFactory {
        private final AstIRFactory<T> astFactory;

        /**
        public AstDefaultIRFactory() {
            this(new AstIRFactory<>());
        }
        */
        
        public AstDefaultIRFactory(AstIRFactory<T> astFactory) {
            this.astFactory = astFactory;
        }

        /**
        @Override
        public IR makeIR(IMethod method, Context context, SSAOptions options) {
            if (method instanceof AstMethod) {
                return astFactory.makeIR(method, context, options);
            } else {
                return super.makeIR(method, context, options);
            }
        }
        */

        @Override
        public IR makeIR(IMethod method, Context context, SSAOptions options) {
            if (enableCOPAnalysis) {
                System.out.println("COP Analysis Enabled");
            }
            return super.makeIR(method, context, options);
        }

        @Override
        public ControlFlowGraph<?, ?> makeCFG(IMethod method, Context context) {
            if (method instanceof AstMethod) {
                return astFactory.makeCFG(method);
            } else {
                return super.makeCFG(method, context);
            }
        }
    }

    public static class AstIR extends IR {
        private final LexicalInformation lexicalInfo;
        private final SSAConversion.SSAInformation localMap;

        public LexicalInformation lexicalInfo() {
            return lexicalInfo;
        }

        private AstIR(AstMethod method, SSAInstruction[] instructions, SymbolTable symbolTable, SSACFG cfg, SSAOptions options) {
            super(method, instructions, symbolTable, cfg, options);

            lexicalInfo = method.cloneLexicalInfo();
            setCatchInstructions(getControlFlowGraph(), method.cfg());
            localMap = SSAConversion.convert(method, this, options);
            setupCatchTypes(getControlFlowGraph(), method.catchTypes());
            setupLocationMap();

            // Analyze and process COP constructs during IR creation
            processCOPInstructions();
        }

        private void setCatchInstructions(SSACFG ssacfg, AbstractCFG<?, ?> oldcfg) {
            for (int i = 0; i < oldcfg.getNumberOfNodes(); i++) {
                if (oldcfg.isCatchBlock(i)) {
                    ExceptionHandlerBasicBlock B = (ExceptionHandlerBasicBlock) ssacfg.getNode(i);
                    B.setCatchInstruction(
                        (SSAGetCaughtExceptionInstruction) getInstructions()[B.getFirstInstructionIndex()]
                    );
                    getInstructions()[B.getFirstInstructionIndex()] = null;
                }
            }
        }

        private void processCOPInstructions() {
            for (SSAInstruction inst : getInstructions()) {
                if (inst instanceof AstGlobalWrite || inst instanceof AstPropertyWrite) {
                    System.out.println("COP instruction detected: " + inst);
                }
            }
        }

        @Override
        protected SSAIndirectionData<Name> getIndirectionData() {
            return null;
        }

        @Override
        public AstMethod getMethod() {
            return (AstMethod) super.getMethod();
        }
    }

    @Override
    public IR makeIR(final IMethod method, final Context context, final SSAOptions options) {
        assert method instanceof AstMethod : method.toString();

        AbstractCFG<?, ?> oldCfg = ((AstMethod) method).cfg();
        SSAInstruction[] oldInstrs = (SSAInstruction[]) oldCfg.getInstructions();
        SSAInstruction[] instrs = oldInstrs.clone();

        // Analyze for COP constructs and handle them
        for (int i = 0; i < instrs.length; i++) {
            if (instrs[i] instanceof JavaScriptInvoke) {
                JavaScriptInvoke invoke = (JavaScriptInvoke) instrs[i];
                String functionName = invoke.getDeclaredTarget().getName().toString();
                if (functionName.equals("adapt")) {
                    instrs[i] = handleContextAdaptation(invoke);
                } else if (functionName.equals("activate")) {
                    instrs[i] = handleContextActivation(invoke);
                }
            }
        }

        IR newIR = new AstIR(
            (AstMethod) method,
            instrs,
            ((AstMethod) method).symbolTable().copy(),
            new SSACFG(method, oldCfg, instrs),
            options
        );

        return newIR;
    }

    private SSAInstruction handleContextAdaptation(JavaScriptInvoke invoke) {
        int objectVar = invoke.getUse(1);  // Object being adapted
        int traitVar = invoke.getUse(2);   // Trait being applied

        // Generate a new global write instruction to simulate adaptation
        AstGlobalWrite rewriteInst = new AstGlobalWrite(
            invoke.iIndex(),  // Instruction index
            objectVar,        // Object being adapted
            traitVar          // Trait applied
        );

        System.out.println("Handling context adaptation for objectVar: " + objectVar);
        return rewriteInst;
    }

    private SSAInstruction handleContextActivation(JavaScriptInvoke invoke) {
        int contextVar = invoke.getUse(1);  // Context being activated

        // Generate a new property write instruction to simulate activation
        AstPropertyWrite rewriteInst = new AstPropertyWrite(
            invoke.iIndex(),  // Instruction index
            contextVar,       // Context being activated
            invoke.getUse(0)  // Global object reference
        );

        System.out.println("Handling context activation for contextVar: " + contextVar);
        return rewriteInst;
    }

    public static IRFactory<IMethod> makeDefaultFactory() {
        return new AstDefaultIRFactory<>(new AstIRFactory<>());
    }

    @Override
    public boolean contextIsIrrelevant(IMethod method) {
        return true;
    }
}
