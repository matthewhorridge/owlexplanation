package org.semanticweb.owl.explanation.impl.blackbox;

import org.semanticweb.owl.explanation.api.ExplanationGeneratorInterruptedException;
import org.semanticweb.owl.explanation.api.ExplanationProgressMonitor;
import org.semanticweb.owlapi.model.OWLAxiom;

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
 * Date: 24-May-2009
 *
 * This contraction strategy is based on Algorithm 2 presented in Baader and Suntisrivaraporn
 * in "Debugging Snomed CT Using Axiom Pinpointing in the Description Logic EL+".
 */
public class DivideAndConquerContractionStrategy implements ContractionStrategy {

    private int count;

    public Set<OWLAxiom> doPruning(Set<OWLAxiom> axioms, EntailmentChecker checker, ExplanationProgressMonitor<?> progressMonitor) {
        count = 0;
        List<OWLAxiom> axiomList = new ArrayList<OWLAxiom>(axioms);
        List<OWLAxiom> result = extract(new ArrayList<OWLAxiom>(), axiomList, checker, progressMonitor);
        return new HashSet<OWLAxiom>(result);
    }

    public List<OWLAxiom> extract(List<OWLAxiom> listS, List<OWLAxiom> listO, EntailmentChecker checker, ExplanationProgressMonitor<?> progressMonitor) {
        if(progressMonitor.isCancelled()) {
            throw new ExplanationGeneratorInterruptedException();
        }
        if(listO.size() == 1) {
            return listO;
        }
        ListHalves halves = getHalves(listO);
        if(isEntailed(listS, halves.getS1(), checker)) {
            return extract(listS, halves.getS1(), checker, progressMonitor);
        }
        if(isEntailed(listS, halves.getS2(), checker)) {
            return extract(listS, halves.getS2(), checker, progressMonitor);
        }
        List<OWLAxiom> listSWithS2 = new ArrayList<OWLAxiom>(listS);
        listSWithS2.addAll(halves.getS2());
        List<OWLAxiom> listS1Prime = extract(listSWithS2, halves.getS1(), checker, progressMonitor);

        List<OWLAxiom> listSWithS1Prime = new ArrayList<OWLAxiom>(listS);
        listSWithS1Prime.addAll(listS1Prime);
        List<OWLAxiom> listS2Prime = extract(listSWithS1Prime, halves.getS2(), checker, progressMonitor);
        List<OWLAxiom> listS1PrimeWithS2Prime = new ArrayList<OWLAxiom>(listS1Prime);
        listS1PrimeWithS2Prime.addAll(listS2Prime);
        return listS1PrimeWithS2Prime;
    }

    private boolean isEntailed(List<OWLAxiom> listA, List<OWLAxiom> listB, EntailmentChecker checker) {
        count++;
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>((int)((listA.size() + listB.size()) * 1.3));
        axioms.addAll(listA);
        axioms.addAll(listB);
        return checker.isEntailed(axioms);
    }

    private ListHalves getHalves(List<OWLAxiom> axioms) {
        return new ListHalves(axioms);
    }

    public int getNumberOfSteps() {
        return count;
    }

    private class ListHalves {

        private List<OWLAxiom> listS1;

        private List<OWLAxiom> listS2;


        public ListHalves(List<OWLAxiom> input) {
            int listASize = input.size() / 2;
            listS1 = new ArrayList<OWLAxiom>(input.subList(0, listASize));
            listS2 = new ArrayList<OWLAxiom>(input.subList(listASize, input.size()));
        }

        public List<OWLAxiom> getS1() {
            return listS1;
        }

        public List<OWLAxiom> getS2() {
            return listS2;
        }
    }
}
