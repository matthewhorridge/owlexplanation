package org.semanticweb.owl.explanation.impl.util;

import org.semanticweb.owl.explanation.impl.laconic.TauGenerator;
import org.semanticweb.owlapi.model.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 23/04/2011
 */
public class DeltaPlusTransformation implements AxiomTransformation {

    private OWLDataFactory dataFactory;

    private int cardinalityBound;

    private ModularityStrategy modularityStrategy = ModularityStrategy.DONT_USE_MODULARITY;

    public DeltaPlusTransformation(OWLDataFactory dataFactory, int cardinalityBound, ModularityStrategy modularityStrategy) {
        this.dataFactory = dataFactory;
        this.modularityStrategy = modularityStrategy;
    }

    public DeltaPlusTransformation(OWLDataFactory dataFactory) {
        this(dataFactory, 0, ModularityStrategy.DONT_USE_MODULARITY);
    }

    public Set<OWLAxiom> transform(Set<OWLAxiom> axioms) {
        DeltaTransformation transformation = new DeltaTransformation(dataFactory);
        Set<OWLAxiom> deltaAxioms = transformation.transform(axioms);
        Set<OWLAxiom> deltaPlusAxioms = new HashSet<OWLAxiom>(deltaAxioms);
        DeltaAxiomVisitor axiomVisitor = new DeltaAxiomVisitor();
        for(OWLAxiom deltaAxiom : deltaAxioms) {
            deltaPlusAxioms.addAll(deltaAxiom.accept(axiomVisitor));
        }
        return deltaPlusAxioms;
    }
    
    
    private class DeltaAxiomVisitor implements OWLAxiomVisitorEx<Set<OWLAxiom>> {

        public Set<OWLAxiom> visit(OWLSubClassOfAxiom axiom) {
            Set<OWLClassExpression> superClses = new HashSet<OWLClassExpression>();
            PositiveClassExpressionWeakener positiveWeakener = new PositiveClassExpressionWeakener();
            superClses.addAll(axiom.getSuperClass().accept(positiveWeakener));
            Set<OWLClassExpression> subClses = new HashSet<OWLClassExpression>();
            NegativeClassExpressionWeakener negativeWeakener = new NegativeClassExpressionWeakener();
            subClses.addAll(axiom.getSubClass().accept(negativeWeakener));
            Set<OWLAxiom> result = new HashSet<OWLAxiom>();
            for(OWLClassExpression superCls : superClses) {
                for(OWLClassExpression subCls : subClses) {
                    OWLSubClassOfAxiom ax = dataFactory.getOWLSubClassOfAxiom(subCls, superCls, axiom.getAnnotations());
                    result.add(ax);
                }
            }
            return result;
        }

        public Set<OWLAxiom> visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLAsymmetricObjectPropertyAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLReflexiveObjectPropertyAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLDisjointClassesAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLDataPropertyDomainAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLObjectPropertyDomainAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLEquivalentObjectPropertiesAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLDifferentIndividualsAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLDisjointDataPropertiesAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLDisjointObjectPropertiesAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLObjectPropertyRangeAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLObjectPropertyAssertionAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLFunctionalObjectPropertyAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLSubObjectPropertyOfAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLDisjointUnionAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLDeclarationAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLAnnotationAssertionAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLSymmetricObjectPropertyAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLDataPropertyRangeAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLFunctionalDataPropertyAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLEquivalentDataPropertiesAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLClassAssertionAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLEquivalentClassesAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLDataPropertyAssertionAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLTransitiveObjectPropertyAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLSubDataPropertyOfAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLSameIndividualAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLSubPropertyChainOfAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLInverseObjectPropertiesAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLHasKeyAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLDatatypeDefinitionAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(SWRLRule rule) {
            return Collections.<OWLAxiom>singleton(rule);
        }

        public Set<OWLAxiom> visit(OWLSubAnnotationPropertyOfAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLAnnotationPropertyDomainAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLAnnotationPropertyRangeAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }
    }
    
    
    private class PositiveClassExpressionWeakener implements OWLClassExpressionVisitorEx<Set<OWLClassExpression>> {

        public Set<OWLClassExpression> visit(OWLClass ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLObjectIntersectionOf ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLObjectUnionOf ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLObjectComplementOf ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLObjectSomeValuesFrom ce) {
            Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
            result.add(ce);
            result.add(dataFactory.getOWLObjectSomeValuesFrom(ce.getProperty(), dataFactory.getOWLThing()));
            return result;
        }

        public Set<OWLClassExpression> visit(OWLObjectAllValuesFrom ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLObjectHasValue ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLObjectMinCardinality ce) {
            Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
            for(int n = ce.getCardinality(); n > 0; n--) {
                result.add(dataFactory.getOWLObjectMinCardinality(n, ce.getProperty(), ce.getFiller()));
                result.add(dataFactory.getOWLObjectMinCardinality(n, ce.getProperty(), dataFactory.getOWLThing()));
            }
            return result;
        }

        public Set<OWLClassExpression> visit(OWLObjectExactCardinality ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLObjectMaxCardinality ce) {
            Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
            for(int n = ce.getCardinality(); n < cardinalityBound; n++) {
                result.add(dataFactory.getOWLObjectMaxCardinality(n, ce.getProperty(), ce.getFiller()));
                result.add(dataFactory.getOWLObjectMaxCardinality(n, ce.getProperty(), dataFactory.getOWLThing()));
            }
            return result;
        }

        public Set<OWLClassExpression> visit(OWLObjectHasSelf ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLObjectOneOf ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLDataSomeValuesFrom ce) {
            Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
            result.add(ce);
//            result.add(dataFactory.getOWLDataSomeValuesFrom(ce.getProperty(), dataFactory.getTopDatatype()));
            return result;
        }

        public Set<OWLClassExpression> visit(OWLDataAllValuesFrom ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLDataHasValue ce) {
            Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
            result.add(ce);
//            result.add(dataFactory.getOWLDataSomeValuesFrom(ce.getProperty(), dataFactory.getTopDatatype()));
            return result;
        }

        public Set<OWLClassExpression> visit(OWLDataMinCardinality ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLDataExactCardinality ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLDataMaxCardinality ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }
    }


    private class NegativeClassExpressionWeakener implements OWLClassExpressionVisitorEx<Set<OWLClassExpression>> {

        public Set<OWLClassExpression> visit(OWLClass ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLObjectIntersectionOf ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLObjectUnionOf ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLObjectComplementOf ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLObjectSomeValuesFrom ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLObjectAllValuesFrom ce) {
            Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
            result.add(ce);
            result.add(dataFactory.getOWLObjectAllValuesFrom(ce.getProperty(), dataFactory.getOWLNothing()));
            return result;
        }

        public Set<OWLClassExpression> visit(OWLObjectHasValue ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLObjectMinCardinality ce) {
            Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
            for(int n = ce.getCardinality(); n < cardinalityBound; n++) {
                result.add(dataFactory.getOWLObjectMinCardinality(n, ce.getProperty(), ce.getFiller()));
            }
            return result;
        }

        public Set<OWLClassExpression> visit(OWLObjectExactCardinality ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLObjectMaxCardinality ce) {
            Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
            result.add(ce);
            for(int n = ce.getCardinality(); n > 0; n--) {
                result.add(dataFactory.getOWLObjectMaxCardinality(n, ce.getProperty(), ce.getFiller()));
            }
            return result;
        }

        public Set<OWLClassExpression> visit(OWLObjectHasSelf ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLObjectOneOf ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLDataSomeValuesFrom ce) {

            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLDataAllValuesFrom ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLDataHasValue ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLDataMinCardinality ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLDataExactCardinality ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }

        public Set<OWLClassExpression> visit(OWLDataMaxCardinality ce) {
            return Collections.<OWLClassExpression>singleton(ce);
        }
    }
}
