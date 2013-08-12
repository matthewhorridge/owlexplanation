package org.semanticweb.owl.explanation.impl.blackbox;

import org.semanticweb.owl.explanation.api.ExplanationProgressMonitor;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;/*
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
 * Author: Matthew Horridge<br> The University of Manchester<br> Information Management Group<br>
 * Date: 27-Nov-2008
 */
public class OrderedAxiomWithWindowContractionStrategy implements ContractionStrategy {

    private Object lastEntailment;

    private int cumulativeExpansionSize;

    private int cumulativeJustificationSize;

    private double justificationToExpansionRatio;

    private Set<OWLAxiom> lastJustification;

    private int count = 0;


    public OrderedAxiomWithWindowContractionStrategy() {
        lastJustification = new HashSet<OWLAxiom>();
    }

    public Set<OWLAxiom> doPruning(Set<OWLAxiom> axioms, EntailmentChecker checker, ExplanationProgressMonitor<?> progressMonitor) {

        count = 0;
        int windowSize = 10;

        List<OWLAxiom> orderedAxioms = new ArrayList<OWLAxiom>(axioms);
        if(lastEntailment != null) {
            if(checker.getEntailment().equals(lastEntailment)) {

             if(!lastJustification.isEmpty()) {
             }
        }
            else {
                lastJustification.clear();
            }
        }

        int windowCount = orderedAxioms.size() / windowSize;
        Set<OWLAxiom> contraction = new HashSet<OWLAxiom>(axioms);

        for(int windowIndex = 0; windowIndex < windowCount; windowIndex++) {
            int start = windowIndex * windowSize;
            int end = start + windowSize;
            List<OWLAxiom> cur = orderedAxioms.subList(start, end);
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

        lastEntailment = checker.getEntailment();
        cumulativeJustificationSize += contraction.size();
        cumulativeExpansionSize += axioms.size();
        justificationToExpansionRatio = (1.0 * cumulativeJustificationSize) / (1.0 * cumulativeExpansionSize);
        lastJustification.clear();
                return contraction;
    }

    public int getNumberOfSteps() {
        return count;
    }
}
