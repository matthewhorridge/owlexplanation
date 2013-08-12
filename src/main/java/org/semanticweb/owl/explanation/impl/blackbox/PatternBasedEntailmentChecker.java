package org.semanticweb.owl.explanation.impl.blackbox;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owlapi.model.*;

import java.util.*;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 14/03/2011
 */
public class PatternBasedEntailmentChecker {

    private OWLAxiom entailment;

    private OWLSubClassOfAxiom subClassOfEntailment;

    private OWLClassAssertionAxiom classAssertionEntailment;

    private Set<Explanation<OWLAxiom>> simpleExplanations = new HashSet<Explanation<OWLAxiom>>();

    public PatternBasedEntailmentChecker(OWLAxiom entailment, Set<OWLAxiom> workingAxioms) {
        this.entailment = entailment;
        if (entailment instanceof OWLSubClassOfAxiom) {
            subClassOfEntailment = (OWLSubClassOfAxiom) entailment;
        }
        else {
            subClassOfEntailment = null;
        }
        if (entailment instanceof OWLClassAssertionAxiom) {
            classAssertionEntailment = (OWLClassAssertionAxiom) entailment;
        }
        else {
            classAssertionEntailment = null;
        }
        processAxioms(workingAxioms);
    }

    public Set<Explanation<OWLAxiom>> getSimpleExplanations() {
        return simpleExplanations;
    }

    private void processAxioms(Set<OWLAxiom> axioms) {
        WorkingAxiomVisitor visitor = new WorkingAxiomVisitor();
        for (OWLAxiom ax : axioms) {
            if (entailment.equalsIgnoreAnnotations(ax)) {
                Set<OWLAxiom> axs = Collections.singleton(ax);
                simpleExplanations.add(new Explanation<OWLAxiom>(entailment, axs));
            }
            ax.accept(visitor);
        }
    }

    private void addExplanation(OWLAxiom explanationAxiom) {

    }


    private class WorkingAxiomVisitor implements OWLAxiomVisitor {

        private Map<OWLObjectPropertyExpression, Set<OWLAxiom>> existentials = new HashMap<OWLObjectPropertyExpression, Set<OWLAxiom>>();


        public void visit(OWLDeclarationAxiom axiom) {
        }

        public void visit(OWLSubClassOfAxiom axiom) {
            if (subClassOfEntailment != null) {
                Set<OWLClassExpression> superConjuncts = axiom.getSuperClass().asConjunctSet();
                Set<OWLClassExpression> subDisjuncts = axiom.getSubClass().asDisjunctSet();
                if (superConjuncts.contains(axiom.getSuperClass())) {
                    if (subDisjuncts.contains(axiom.getSubClass())) {
                        addExplanation(axiom);
                    }
                }
            }
        }

        public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {

        }

        public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        }

        public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
        }

        public void visit(OWLDisjointClassesAxiom axiom) {
        }

        public void visit(OWLDataPropertyDomainAxiom axiom) {
        }

        public void visit(OWLObjectPropertyDomainAxiom axiom) {
        }

        public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        }

        public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        }

        public void visit(OWLDifferentIndividualsAxiom axiom) {
        }

        public void visit(OWLDisjointDataPropertiesAxiom axiom) {
        }

        public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
        }

        public void visit(OWLObjectPropertyRangeAxiom axiom) {
        }

        public void visit(OWLObjectPropertyAssertionAxiom axiom) {
            OWLObjectPropertyExpression prop = axiom.getProperty().getSimplified();
            Set<OWLAxiom> axioms = existentials.get(prop);
            if (axioms == null) {
                axioms = new HashSet<OWLAxiom>();
                existentials.put(prop, axioms);
            }
            axioms.add(axiom);
        }

        public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
        }

        public void visit(OWLSubObjectPropertyOfAxiom axiom) {
        }

        public void visit(OWLDisjointUnionAxiom axiom) {
        }

        public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
        }

        public void visit(OWLDataPropertyRangeAxiom axiom) {
        }

        public void visit(OWLFunctionalDataPropertyAxiom axiom) {
        }

        public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
        }

        public void visit(OWLClassAssertionAxiom axiom) {

        }

        public void visit(OWLEquivalentClassesAxiom axiom) {
            // For SubClassOf(C, D)
            // Handle axioms of the form
            // EquivalentClasses(C, D)
            // EquivalentClasses(C, ObjectIntersectionOf(D, ...))
            // EquivalentClasses(D, ObjectUnionOf(C, ...))
            if (subClassOfEntailment != null) {
                OWLClassExpression subClass = subClassOfEntailment.getSubClass();
                OWLClassExpression superClass = subClassOfEntailment.getSuperClass();
                if (axiom.contains(subClass) && axiom.contains(superClass)) {
                    addExplanation(axiom);
                }
                else if (axiom.contains(subClass)) {
                    for (OWLClassExpression ce : axiom.getClassExpressions()) {
                        if (!ce.equals(subClass)) {
                            if (ce.asConjunctSet().contains(superClass)) {
                                addExplanation(axiom);
                                break;
                            }
                        }
                    }
                }
                else if (axiom.contains(superClass)) {
                    for (OWLClassExpression ce : axiom.getClassExpressions()) {
                        if (!ce.equals(superClass)) {
                            if (ce.asDisjunctSet().contains(subClass)) {
                                addExplanation(axiom);
                                break;
                            }
                        }
                    }
                }
            }
        }

        public void visit(OWLDataPropertyAssertionAxiom axiom) {

        }

        public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
        }

        public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        }

        public void visit(OWLSubDataPropertyOfAxiom axiom) {
        }

        public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        }

        public void visit(OWLSameIndividualAxiom axiom) {
        }

        public void visit(OWLSubPropertyChainOfAxiom axiom) {
        }

        public void visit(OWLInverseObjectPropertiesAxiom axiom) {
        }

        public void visit(OWLHasKeyAxiom axiom) {
        }

        public void visit(OWLDatatypeDefinitionAxiom axiom) {
        }

        public void visit(SWRLRule rule) {
        }

        public void visit(OWLAnnotationAssertionAxiom axiom) {
        }

        public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
        }

        public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
        }

        public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
        }
    }
}
