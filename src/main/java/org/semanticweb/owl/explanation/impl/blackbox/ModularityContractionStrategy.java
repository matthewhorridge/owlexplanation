package org.semanticweb.owl.explanation.impl.blackbox;

import org.semanticweb.owl.explanation.api.ExplanationProgressMonitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.explanation.ordering.Tree;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.util.*;
/*
 * Copyright (C) 2008, University of Manchester
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


/**
 * Author: Matthew Horridge<br> The University Of Manchester<br> Information Management Group<br> Date:
 * 25-Sep-2008<br><br>
 */
public class ModularityContractionStrategy implements ContractionStrategy {

    private int count = 0;

    private int windowSize;

    private int counter = 0;

    private static void toList(Tree<OWLAxiom> tree, List<OWLAxiom> axioms, EntailmentChecker checker) {
        OWLAxiom axiom = tree.getUserObject();
        if (!axiom.equals(checker.getEntailment())) {
            axioms.add(axiom);
        }
        for (Tree<OWLAxiom> t : tree.getChildren()) {
            toList(t, axioms, checker);
        }
    }


    public Set<OWLAxiom> doPruning(Set<OWLAxiom> axioms, EntailmentChecker checker, ExplanationProgressMonitor<?> progressMonitor) {
        count = 0;
        windowSize = axioms.size() / 20;
        if (windowSize == 0) {
            windowSize = 1;
        }

        List<OWLAxiom> axiomList = new ArrayList<OWLAxiom>(axioms);
        Set<OWLAxiom> contraction = new HashSet<OWLAxiom>(axioms);


        int cursor = 0;
        while (cursor < axiomList.size()) {
            ArrayList<OWLAxiom> picked = new ArrayList<OWLAxiom>(12);
            cursor = pickAxioms(axiomList, cursor, picked) + 1;
            if(!contraction.removeAll(picked)) {
                continue;
            }
            count++;
            if (!checker.isEntailed(contraction)) {
                contraction.addAll(picked);
            }
            else {
                // Compute module - remove anything that isn't in the module
                Set<OWLAxiom> module = computeModule(contraction, checker);
                if(module.size() != contraction.size()) {
                    contraction.clear();
                    contraction.addAll(module);
                    for(int i = 0; i < axiomList.size(); i++) {
                        if(!module.contains(axiomList.get(i))) {
                            axiomList.set(i, null);
                        }
                    }
                }
            }
        }

        counter++;

        // Slow
        Set<OWLAxiom> contractionCopy = new HashSet<OWLAxiom>(contraction);
        for (OWLAxiom ax : contractionCopy) {
            count++;
            contraction.remove(ax);
            if (!checker.isEntailed(contraction)) {
                contraction.add(ax);
            }
        }
        return contraction;
    }

    public int getNumberOfSteps() {
        return count;
    }

    private Set<OWLAxiom> computeModule(Set<OWLAxiom> contraction, EntailmentChecker checker) {
        try {
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
            OWLOntology ont = man.createOntology(contraction);
            SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(man, ont, ModuleType.BOT);
            return extractor.extract(checker.getEntailmentSignature());
        }
        catch (OWLOntologyCreationException e) {
            throw new OWLRuntimeException(e);
        }
    }

    private int pickAxioms(List<OWLAxiom> axs, int start, Collection<OWLAxiom> picked) {
        int finished = 0;
        int added = 0;
        for(int i = start; i < axs.size(); i++) {
            OWLAxiom curAx = axs.get(i);
            if(curAx != null) {
                picked.add(curAx);
                axs.set(i, null);
                added++;
                if(added == windowSize) {
                    break;
                }
            }
            finished = i;
        }
        return finished;
    }


//    private void dumpDiff(Set<OWLAxiom> fast, Set<OWLAxiom> slow) {
//        System.out.println("");
//        System.out.println("");
//        System.out.println("***************************************************************************");
//        System.out.println("***************************************************************************");
//        for (OWLAxiom ax : new TreeSet<OWLAxiom>(fast)) {
//            if (!slow.contains(ax)) {
//                System.out.print("\t\t");
//            }
//            System.out.println(ax);
//        }
//        System.out.println("");
//        System.out.println("");
//        System.out.println("");
//        System.out.println("");
//    }
}
