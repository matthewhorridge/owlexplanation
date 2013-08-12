package org.semanticweb.owl.explanation.impl.blackbox;

import org.semanticweb.owl.explanation.api.ExplanationProgressMonitor;
import org.semanticweb.owlapi.model.*;

import java.util.*;
/*
 * Copyright (C) 2009, University of Manchester
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
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Information Management Group<br>
 * Date: 06-May-2009
 */
public class DynamicSlidingWindowContractionStrategy implements ContractionStrategy {

    final private int windowSize;

    private int count;

    public DynamicSlidingWindowContractionStrategy() {
        windowSize = 20;
    }





    public Set<OWLAxiom> doPruning(Set<OWLAxiom> axioms, EntailmentChecker checker, ExplanationProgressMonitor<?> progressMonitor) {
        count = 0;


        Set<OWLAxiom> contraction = new HashSet<OWLAxiom>(axioms);

        int lastContractionSize = -1;
        while (true) {
            int roundWindowSize = contraction.size() / 20;
            if(roundWindowSize < windowSize) {
                break;
            }
            if(lastContractionSize == contraction.size()) {
//                System.out.println("Contraction not succeeded: Contraction: " + contraction.size() + "    Window size: " + roundWindowSize);
                roundWindowSize = contraction.size() / 40;
            }
            lastContractionSize = contraction.size();
            doFastPruning(checker, contraction, roundWindowSize);
//            System.out.println("Contraction: " + contraction.size());
        }

        // Repeat fast pruning
        doFastPruning(checker, contraction, windowSize);


        // Slow
        Set<OWLAxiom> contractionCopy = new HashSet<OWLAxiom>(contraction);
        for(OWLAxiom ax : contractionCopy) {
            contraction.remove(ax);
            count++;
            if(!checker.isEntailed(contraction)) {
                contraction.add(ax);
            }
        }

        return contraction;
    }

    public int getNumberOfSteps() {
        return count;
    }

    private void doFastPruning(EntailmentChecker checker,
                               Set<OWLAxiom> contraction,
                               int roundWindowSize) {
        List<OWLAxiom> axiomList = new ArrayList<OWLAxiom>(contraction);
        int windowCount = axiomList.size() / roundWindowSize;
        for(int windowIndex = 0; windowIndex < windowCount; windowIndex++) {
            int start = windowIndex * roundWindowSize;
            int end = start + roundWindowSize;
            List<OWLAxiom> cur = axiomList.subList(start, end);
            contraction.removeAll(cur);
            count++;
            if(!checker.isEntailed(contraction)) {
                contraction.addAll(cur);
//                System.out.println("    Failed on window size of " + roundWindowSize + " on " + contraction.size());
            }
            else {
//                System.out.println("    Removed");
            }
        }
    }
}

