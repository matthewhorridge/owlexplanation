package org.semanticweb.owl.explanation.impl.blackbox;

import org.semanticweb.owl.explanation.api.ExplanationGeneratorInterruptedException;
import org.semanticweb.owl.explanation.api.ExplanationProgressMonitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
 * 16-Sep-2008<br><br>
 */
public class StructuralTypePriorityExpansionStrategy implements ExpansionStrategy {

    private int count = 0;

    private InitialEntailmentCheckStrategy initialEntailmentCheckStrategy = InitialEntailmentCheckStrategy.PERFORM;

    public StructuralTypePriorityExpansionStrategy() {
    }

    public StructuralTypePriorityExpansionStrategy(InitialEntailmentCheckStrategy initialEntailmentCheckStrategy) {
        this.initialEntailmentCheckStrategy = initialEntailmentCheckStrategy;
    }

    public Set<OWLAxiom> doExpansion(Set<OWLAxiom> axioms, EntailmentChecker checker, ExplanationProgressMonitor<?> progressMonitor) {

        Set<OWLAxiom> expansion;
        try {
            count = 0;

            if(progressMonitor.isCancelled()) {
                throw new ExplanationGeneratorInterruptedException();
            }

            count++;
            if (initialEntailmentCheckStrategy.equals(InitialEntailmentCheckStrategy.PERFORM)) {
                if(!checker.isEntailed(axioms)) {
                    return Collections.emptySet();
                }
            }

            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
            OWLOntology ont = man.createOntology(axioms);
//            createOntology(axioms, checker);


            expansion = new HashSet<OWLAxiom>();
            Set<OWLEntity> entailmentSignature = checker.getEntailmentSignature();
            EntityFilteredDefinitionExpander expander = new EntityFilteredDefinitionExpander(ont);
            Set<OWLEntity> expandedWithDefinition = new HashSet<OWLEntity>();
            Set<OWLAxiom> addedAxioms = new HashSet<OWLAxiom>();
            for(OWLEntity ent : entailmentSignature) {
                OWLDeclarationAxiom declAx = man.getOWLDataFactory().getOWLDeclarationAxiom(ent);
                expansion.add(declAx);
                addedAxioms.add(declAx);
            }


//        // Initial expansion
            for (OWLEntity ent : entailmentSignature) {
                expandedWithDefinition.add(ent);
                Set<? extends OWLAxiom> owlAxioms = ent.accept(expander);
                expansion.addAll(owlAxioms);
            }
            int size = 0;
            Set<OWLDisjointClassesAxiom> disjointClassesAxioms = new HashSet<OWLDisjointClassesAxiom>();
            Set<OWLEntity> expansionSig = new HashSet<OWLEntity>();
            while (size != expansion.size()) {
                if(progressMonitor.isCancelled()) {
                    return Collections.emptySet();
                }
                size = expansion.size();

                // Add in
                Set<OWLAxiom> combined = new HashSet<OWLAxiom>(disjointClassesAxioms.size() + expansion.size() + 50);
                combined.addAll(expansion);
                for(OWLDisjointClassesAxiom disjointAx : disjointClassesAxioms) {
                    for(OWLClassExpression desc : disjointAx.getClassExpressions()) {
                        if(desc.isAnonymous()) {
                            combined.add(disjointAx);
                            break;
                        }
                        else {
                            if(!expansionSig.contains(desc.asOWLClass())) {
                                break;
                            }
                        }
                        combined.add(disjointAx);
                    }
                }
                count++;
                if (checker.isEntailed(combined)) {
                    Set<OWLAxiom> result = new HashSet<OWLAxiom>(checker.getEntailingAxioms(combined));
                    result.removeAll(addedAxioms);
                    return result;
                }
                // Expand more

                for (OWLAxiom ax : new ArrayList<OWLAxiom>(expansion)) {
                    for (OWLEntity ent : ax.getSignature()) {
                        if (!expandedWithDefinition.contains(ent)) {
                            Set<? extends OWLAxiom> owlAxioms = ent.accept(expander);
                            for (OWLAxiom expAx : owlAxioms) {
                                if (!expAx.getAxiomType().equals(AxiomType.DISJOINT_CLASSES) && !expAx.isOfType(AxiomType.CLASS_ASSERTION, AxiomType.OBJECT_PROPERTY_ASSERTION, AxiomType.DATA_PROPERTY_ASSERTION, AxiomType.SAME_INDIVIDUAL, AxiomType.DIFFERENT_INDIVIDUALS)) {
                                    expansion.add(expAx);
                                    expansionSig.addAll(expAx.getSignature());
                                }
                                else {
                                    if (expAx instanceof OWLDisjointClassesAxiom) {
                                        disjointClassesAxioms.add((OWLDisjointClassesAxiom) expAx);
                                    }
                                }
                            }
                            expandedWithDefinition.add(ent);
                        }
                    }
                }
            }
            for(OWLEntity ent : expansionSig) {
                if (ent.isOWLClass()) {
                    expansion.addAll(ont.getDisjointClassesAxioms(ent.asOWLClass()));
                }
            }
            count++;
            if(checker.isEntailed(expansion)) {
                Set<OWLAxiom> result = new HashSet<OWLAxiom>(checker.getEntailingAxioms(expansion));
                result.removeAll(addedAxioms);
                return result;
            }
            // Not worked ... now we fall back
            // TODO: Optimise more
            while (!expansion.containsAll(ont.getLogicalAxioms())) {
                if(progressMonitor.isCancelled()) {
                    return Collections.emptySet();
                }
                // Expand more
                for (OWLAxiom ax : new ArrayList<OWLAxiom>(expansion)) {
                    for (OWLEntity ent : ax.getSignature()) {
                            Set<? extends OWLAxiom> owlAxioms = ont.getReferencingAxioms(ent);
                            expansion.addAll(owlAxioms);
                    }
                }
                count++;
                if (checker.isEntailed(expansion)) {
                    Set<OWLAxiom> result = new HashSet<OWLAxiom>(checker.getEntailingAxioms(expansion));
                    result.removeAll(addedAxioms);
                    return result;

                }
            }
        }
        catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
        return expansion;
    }

    public int getNumberOfSteps() {
        return count;
    }

    private class EntityFilteredDefinitionExpander implements OWLEntityVisitorEx<Set<? extends OWLAxiom>> {

        private OWLOntology theOnt;

        private EntityFilteredDefinitionExpander(OWLOntology theOnt) {
            this.theOnt = theOnt;
        }

        public Set<? extends OWLAxiom> visit(OWLClass cls) {
            // Return axioms that define the class
            Set<OWLAxiom> axioms = new HashSet<OWLAxiom>(theOnt.getAxioms(cls));
//            for(OWLAxiom ax : theOnt.getReferencingAxioms(cls)) {
//                if (axioms.contains(ax)) {
//                    if(ax.getAxiomType().equals(AxiomType.EQUIVALENT_CLASSES)) {
//                        OWLEquivalentClassesAxiom eqClsesAx = (OWLEquivalentClassesAxiom) ax;
//                        for(OWLClassExpression desc : eqClsesAx.getClassExpressions()) {
//                            if(desc instanceof OWLObjectUnionOf) {
//                                if(((OWLObjectUnionOf) desc).getOperands().contains(cls)) {
//                                    axioms.add(ax);
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
            return axioms;
        }


        public Set<? extends OWLAxiom> visit(OWLAnnotationProperty owlAnnotationProperty) {
            return Collections.emptySet();
        }


        public Set<? extends OWLAxiom> visit(OWLObjectProperty property) {
            return theOnt.getAxioms(property);
        }


        public Set<? extends OWLAxiom> visit(OWLDataProperty property) {
            return theOnt.getAxioms(property);
        }


        public Set<? extends OWLAxiom> visit(OWLNamedIndividual individual) {
            Set<OWLAxiom> axioms = new HashSet<OWLAxiom>(theOnt.getAxioms(individual));
            for(OWLObjectPropertyAssertionAxiom ax : theOnt.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION)) {
                if(ax.getObject().equals(individual)) {
                    axioms.add(ax);       
                }
            }
            return axioms;
        }


        public Set<? extends OWLAxiom> visit(OWLDatatype dataType) {
            return Collections.emptySet();
        }
    }
}
