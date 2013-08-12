package org.semanticweb.owl.explanation.impl.blackbox.hst;

import org.semanticweb.owl.explanation.api.ConsoleExplanationProgressMonitor;
import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.util.*;
/*
 * Copyright (C) 2010, University of Manchester
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
 * Date: 08-Mar-2010
 */
public class HittingSetTreeCharacteristicsTest {

    public static void main(String[] args) {
//
//        BreadthFirstStrategy breadthFirstStrategy = new BreadthFirstStrategy();
//
//        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//        final List<OWLAxiom> axioms = new ArrayList<OWLAxiom>();
//        int axiomCount = 6;
//        int justificationSize = 2;
//        OWLAxiom entailment = manager.getOWLDataFactory().getOWLDeclarationAxiom(manager.getOWLDataFactory().getOWLThing());
//        final List<Explanation<OWLAxiom>> expls = new ArrayList<Explanation<OWLAxiom>>();
//        for(int i = 0; i < axiomCount; i++) {
//            Set<OWLAxiom> expl = new LinkedHashSet<OWLAxiom>();
//            for (int j = 0; j < justificationSize; j++) {
//                OWLDataFactory df = manager.getOWLDataFactory();
//                OWLDeclarationAxiom ax1 = df.getOWLDeclarationAxiom(df.getOWLClass(IRI.create("http://ont#C" + i + "." + j)));
//                expl.add(ax1);
//                axioms.add(ax1);
//
//                OWLDeclarationAxiom ax = df.getOWLDeclarationAxiom(df.getOWLClass(IRI.create("http://ont#X")));
//                                expl.add(ax);
//                axioms.add(ax);
//            }
//            expls.add(new Explanation<OWLAxiom>(entailment, expl));
//
//        }
//        System.out.println(axioms.size() + " axioms");
//
//        Collections.shuffle(expls);
//
////        Explanation<OWLAxiom> expl = createExplanation(axioms, 0);
//
//        HittingSetTree<OWLAxiom> tree = new HittingSetTree<OWLAxiom>(breadthFirstStrategy, new ConsoleExplanationProgressMonitor<OWLAxiom>());
//
//        tree.buildHittingSetTree(expls.get(0).getEntailment(), Integer.MAX_VALUE, new ExplanationGeneratorMediator<OWLAxiom>() {
//
//
//            int counter = 0;
//
//            Set<OWLAxiom> workingSet = new HashSet<OWLAxiom>(axioms);
//
//            public Explanation<OWLAxiom> generateExplanation(OWLAxiom entailment) {
//                for(Explanation<OWLAxiom> expl : expls) {
//                    if(workingSet.containsAll(expl.getAxioms())) {
//                        return expl;
//                    }
//                }
//                return Explanation.getEmptyExplanation(entailment);
//            }
//
//            public void removeAxiom(OWLAxiom axiom) {
//                workingSet.remove(axiom);
//                Collections.shuffle(expls, new Random());
//
//            }
//
//
//            public void addAxiom(OWLAxiom axiom) {
//                workingSet.add(axiom);
//                Collections.shuffle(expls, new Random(System.currentTimeMillis()));
//            }
//        });
////        StringWriter sw = new StringWriter();
//
//        try {
//            FileWriter fw = new FileWriter(new File("/tmp/testtree.xml"));
//            PrintWriter pw = new PrintWriter(fw);
//            tree.writeTreeML(pw);
//
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }

    }


}
