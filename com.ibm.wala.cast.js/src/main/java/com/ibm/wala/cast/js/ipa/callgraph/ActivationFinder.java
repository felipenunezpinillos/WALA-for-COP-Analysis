package com.ibm.wala.cast.js.ipa.callgraph;

import com.ibm.wala.cast.js.ssa.SSAContextActivateInstruction;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to analyze IR and identify Context.activate calls.
 */
public class ActivationFinder {

    /**
     * Finds all activation instructions in a given IR.
     *
     * @param node The call graph node representing a function/method.
     * @return List of SSAContextActivateInstruction instances found in the IR.
     */
    public static List<SSAContextActivateInstruction> findActivations(CGNode node) {
        List<SSAContextActivateInstruction> activations = new ArrayList<>();
        IR ir = node.getIR();

        if (ir != null) {
            for (SSAInstruction inst : ir.getInstructions()) {
                if (inst instanceof SSAContextActivateInstruction) {
                    activations.add((SSAContextActivateInstruction) inst);
                }
            }
        }

        return activations;
    }

    /**
     * Prints all detected activations within a function.
     *
     * @param node The call graph node to analyze.
     */
    public static void reportActivations(CGNode node) {
        List<SSAContextActivateInstruction> activations = findActivations(node);
        for (SSAContextActivateInstruction activate : activations) {
            System.out.println("Found activation at index " + activate.iIndex() +
                               " activating context " + activate.getContextVar());
        }
    }
}
