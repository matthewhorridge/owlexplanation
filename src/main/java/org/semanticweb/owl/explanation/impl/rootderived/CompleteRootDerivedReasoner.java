package org.semanticweb.owl.explanation.impl.rootderived;

import org.semanticweb.owl.explanation.api.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.util.SimpleRenderer;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.util.*;
import java.net.URI;
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
 * Date: 20-Oct-2009
 */
public class CompleteRootDerivedReasoner implements RootDerivedReasoner {

    private OWLOntologyManager manager;

    private OWLReasoner baseReasoner;

    private OWLReasonerFactory reasonerFactory;

    private Map<OWLClass, Set<Explanation<OWLAxiom>>> cls2JustificationMap;

    private Set<OWLClass> roots = new HashSet<OWLClass>();


    public CompleteRootDerivedReasoner(OWLOntologyManager manager, OWLReasoner baseReasoner, OWLReasonerFactory reasonerFactory) {
        this.manager = manager;
        this.baseReasoner = baseReasoner;
        this.reasonerFactory = reasonerFactory;
    }

    /**
     * Gets the root unsatisfiable classes.
     * @return A set of classes that represent the root unsatisfiable classes
     */
    public Set<OWLClass> getRootUnsatisfiableClasses() throws ExplanationException {
        StructuralRootDerivedReasoner srd = new StructuralRootDerivedReasoner(manager, baseReasoner, reasonerFactory);
        Set<OWLClass> estimatedRoots = srd.getRootUnsatisfiableClasses();
        cls2JustificationMap = new HashMap<OWLClass, Set<Explanation<OWLAxiom>>>();
        Set<OWLAxiom> allAxioms = new HashSet<OWLAxiom>();
        for (OWLOntology ont : baseReasoner.getRootOntology().getImportsClosure()) {
            allAxioms.addAll(ont.getLogicalAxioms());
        }

        for (OWLClass cls : estimatedRoots) {
            cls2JustificationMap.put(cls, new HashSet<Explanation<OWLAxiom>>());
            System.out.println("POTENTIAL ROOT: " + cls);
        }
        System.out.println("Finding real roots from " + estimatedRoots.size() + " estimated roots");

        int done = 0;
        roots.addAll(estimatedRoots);
        for (final OWLClass estimatedRoot : estimatedRoots) {
            ExplanationGeneratorFactory<OWLAxiom> genFac = ExplanationManager.createExplanationGeneratorFactory(reasonerFactory);
            ExplanationGenerator<OWLAxiom> gen = genFac.createExplanationGenerator(allAxioms);
            OWLDataFactory df = manager.getOWLDataFactory();
            Set<Explanation<OWLAxiom>> expls = gen.getExplanations(df.getOWLSubClassOfAxiom(estimatedRoot, df.getOWLNothing()));
            cls2JustificationMap.get(estimatedRoot).addAll(expls);
            done++;
            System.out.println("Done " + done);
        }
        for(OWLClass clsA : estimatedRoots) {
            for(OWLClass clsB : estimatedRoots) {
                if (!clsA.equals(clsB)) {
                    Set<Explanation<OWLAxiom>> clsAExpls = cls2JustificationMap.get(clsA);
                    Set<Explanation<OWLAxiom>> clsBExpls = cls2JustificationMap.get(clsB);
                    boolean clsARootForClsB = false;
                    boolean clsBRootForClsA = false;
                    // Be careful of cyclic dependencies!
                    for(Explanation<OWLAxiom> clsAExpl : clsAExpls) {
                        for(Explanation<OWLAxiom> clsBExpl : clsBExpls) {
                            if(isRootFor(clsAExpl, clsBExpl)) {
                                // A is a root of B
                                clsARootForClsB = true;
//                                System.out.println(clsB + "  --- depends --->  " + clsA);
                            }
                            else if(isRootFor(clsBExpl, clsAExpl)) {
                                // B is a root of A
                                clsBRootForClsA = true;
//                                System.out.println(clsA + "  --- depends --->  " + clsB);
                            }
                        }
                    }
                    if (!clsARootForClsB || !clsBRootForClsA) {
                        if(clsARootForClsB) {
                            roots.remove(clsB);
                        }
                        else if(clsBRootForClsA) {
                            roots.remove(clsA);
                        }
                    }
                }
            }
        }
        return roots;
    }

    private static boolean isRootFor(Explanation<OWLAxiom> explA, Explanation<OWLAxiom> explB) {
        return explB.getAxioms().containsAll(explA.getAxioms()) && !explA.getAxioms().equals(explB.getAxioms());
    }


    public Set<OWLClass> getDependentChildClasses(OWLClass cls) {
        return null;
    }

    public Set<OWLClass> getDependentDescendantClasses(OWLClass cls) {
        return null;
    }


    public static void main(String[] args) {
//        try {
//            SimpleRenderer renderer = new SimpleRenderer();
//            renderer.setShortFormProvider(new DefaultPrefixManager("http://www.mindswap.org/ontologies/tambis-full.owl#"));
//            ToStringRenderer.getInstance().setRenderer(renderer);
//            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
////            OWLOntology ont = man.loadOntologyFromOntologyDocument(URI.create("http://owl.cs.manchester.ac.uk/repository/download?ontology=http://miniTambis&format=RDF/XML"));
//            OWLOntology ont = man.loadOntologyFromOntologyDocument(IRI.create("http://owl.cs.manchester.ac.uk/repository/download?ontology=http://www.cs.manchester.ac.uk/owl/ontologies/tambis-patched.owl&format=RDF/XML"));
//            System.out.println("Loaded!");
//            OWLReasonerFactory reasonerFactory = new PelletReasonerFactory();
//            OWLReasoner reasoner = reasonerFactory.createReasoner(ont);
//            reasoner.getUnsatisfiableClasses();
//            CompleteRootDerivedReasoner rdr = new CompleteRootDerivedReasoner(man, reasoner, reasonerFactory);
//            for(OWLClass cls : rdr.getRootUnsatisfiableClasses()) {
//                System.out.println("ROOT! " + cls);
//            }
//        }
//        catch (OWLOntologyCreationException e) {
//            e.printStackTrace();
//        }
//        catch (ExplanationException e) {
//            e.printStackTrace();
//        }
    }
}
