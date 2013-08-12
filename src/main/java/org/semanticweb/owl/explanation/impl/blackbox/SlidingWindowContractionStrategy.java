package org.semanticweb.owl.explanation.impl.blackbox;

import org.semanticweb.owl.explanation.api.ExplanationProgressMonitor;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 * 03-Sep-2008<br><br>
 *
 * A contraction strategy that uses a sliding window to improve performance.
 */
public class SlidingWindowContractionStrategy implements ContractionStrategy {

    final private int windowSize;

    private int count;

    public SlidingWindowContractionStrategy() {
        windowSize = 20;
    }


    public SlidingWindowContractionStrategy(int windowSize) {
        this.windowSize = windowSize;
    }




    public Set<OWLAxiom> doPruning(Set<OWLAxiom> axioms, EntailmentChecker checker, ExplanationProgressMonitor<?> progressMonitor) {
        count = 0;
        List<OWLAxiom> axiomList = new ArrayList<OWLAxiom>(axioms);
        int windowCount = axiomList.size() / windowSize;
        Set<OWLAxiom> contraction = new HashSet<OWLAxiom>(axioms);
        
        for(int windowIndex = 0; windowIndex < windowCount; windowIndex++) {
            int start = windowIndex * windowSize;
            int end = start + windowSize;
            List<OWLAxiom> cur = axiomList.subList(start, end);
            contraction.removeAll(cur);
            count++;
            if(!checker.isEntailed(contraction)) {
                contraction.addAll(cur);
            }
        }


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
}
