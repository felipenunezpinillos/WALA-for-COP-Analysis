package com.ibm.wala.cast.js.ipa.callgraph;

import com.ibm.wala.cast.js.ssa.SSAContextAdaptInstruction;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to analyze IR and identify Context.adapt calls.
 */
public class AdaptationFinder {

    /**
     * Finds all adaptation instructions in a given IR.
     *
     * @param node The call graph node representing a function/method.
     * @return List of SSAContextAdaptInstruction instances found in the IR.
     */
    public static List<SSAContextAdaptInstruction> findAdaptations(CGNode node) {
        List<SSAContextAdaptInstruction> adaptations = new ArrayList<>();
        IR ir = node.getIR();

        if (ir != null) {
            for (SSAInstruction inst : ir.getInstructions()) {
                if (inst instanceof SSAContextAdaptInstruction) {
                    adaptations.add((SSAContextAdaptInstruction) inst);
                }
            }
        }

        return adaptations;
    }

    /**
     * Prints all detected adaptations within a function.
     *
     * @param node The call graph node to analyze.
     */
    public static void reportAdaptations(CGNode node) {
        List<SSAContextAdaptInstruction> adaptations = findAdaptations(node);
        for (SSAContextAdaptInstruction adapt : adaptations) {
            System.out.println("Found adaptation at index " + adapt.iIndex() + 
                               " adapting object " + adapt.getObjectVar() +
                               " with trait " + adapt.getTraitVar());
        }
    }
}
