package org.semanticweb.owl.explanation.impl.util;

import org.semanticweb.owlapi.model.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 01/03/2011
 */
public class DeltaTransformationUnfolder {

    private OWLDataFactory dataFactory;

    private Map<OWLClass, Set<OWLClassExpression>> posName2ClassExpressionMap = new HashMap<OWLClass, Set<OWLClassExpression>>();

    private Map<OWLClass, Set<OWLClassExpression>> negName2ClassExpressionMap = new HashMap<OWLClass, Set<OWLClassExpression>>();

    public DeltaTransformationUnfolder(OWLDataFactory dataFactory) {
        this.dataFactory = dataFactory;
    }

    public Set<OWLAxiom> getUnfolded(Set<OWLAxiom> axioms, Set<OWLEntity> signature) {
        posName2ClassExpressionMap.clear();
        negName2ClassExpressionMap.clear();
        Set<OWLAxiom> toUnfold = new HashSet<OWLAxiom>();
        for(OWLAxiom ax : axioms) {
            if(ax instanceof OWLSubClassOfAxiom) {
                OWLSubClassOfAxiom sca = (OWLSubClassOfAxiom) ax;
                OWLClassExpression subCls = sca.getSubClass();
                OWLClassExpression superCls = sca.getSuperClass();
                if(isFreshName(subCls, signature) && !isFreshName(superCls, signature)) {
                    addToIndex(subCls.asOWLClass(), superCls, posName2ClassExpressionMap);
                }
                else if(isFreshName(superCls, signature) && !isFreshName(subCls, signature)) {
                    addToIndex(superCls.asOWLClass(), subCls, negName2ClassExpressionMap);
                }
                else {
                    toUnfold.add(ax);
                }
            }
            else {
                toUnfold.add(ax);
            }
        }
        
        Set<OWLAxiom> unfolded = new HashSet<OWLAxiom>();
        AxiomUnfolder axiomUnfolder = new AxiomUnfolder();
        for(OWLAxiom ax : toUnfold) {
            unfolded.add(ax.accept(axiomUnfolder));
        }
        return unfolded;
    }

    private static void addToIndex(OWLClass key, OWLClassExpression val, Map<OWLClass, Set<OWLClassExpression>> map) {
        Set<OWLClassExpression> vals = map.get(key);
        if(vals == null) {
            vals = new HashSet<OWLClassExpression>();
            map.put(key, vals);
        }
        vals.add(val);
    }

    private boolean isFreshName(OWLClassExpression ce, Set<OWLEntity> signature) {
        return !ce.isOWLThing() && !ce.isOWLNothing() && !ce.isAnonymous() && !signature.contains(ce.asOWLClass());
    }

    private class AxiomUnfolder implements OWLAxiomVisitorEx<OWLAxiom> {

        private ClassExpressionUnfolder positiveClassExpressionUnfolder = new ClassExpressionUnfolder(Polarity.POSITIVE);

        private ClassExpressionUnfolder negativeClassExpressionUnfolder = new ClassExpressionUnfolder(Polarity.NEGATIVE);

        private OWLClassExpression unfold(OWLClassExpression ce, Polarity polarity) {
            ClassExpressionUnfolder classExpressionUnfolder;
            if(polarity.isPositive()) {
                classExpressionUnfolder = positiveClassExpressionUnfolder;
            }
            else {
                classExpressionUnfolder = negativeClassExpressionUnfolder;
            }
            return ce.accept(classExpressionUnfolder);
        }

        private Set<OWLClassExpression> unfold(Set<OWLClassExpression> classExpressions, Polarity polarity) {
            Set<OWLClassExpression> unfolded = new HashSet<OWLClassExpression>();
            for(OWLClassExpression ce : classExpressions) {
                if (polarity.isPositive()) {
                    unfolded.add(ce.accept(positiveClassExpressionUnfolder));
                }
                else {
                    unfolded.add(ce.accept(negativeClassExpressionUnfolder));
                }
            }
            return unfolded;
        }


        public OWLAxiom visit(OWLSubClassOfAxiom axiom) {
            return dataFactory.getOWLSubClassOfAxiom(unfold(axiom.getSubClass(), Polarity.NEGATIVE), unfold(axiom.getSuperClass(), Polarity.POSITIVE));
        }

        public OWLAxiom visit(OWLNegativeObjectPropertyAssertionAxiom owlNegativeObjectPropertyAssertionAxiom) {
            return owlNegativeObjectPropertyAssertionAxiom;
        }

        public OWLAxiom visit(OWLAsymmetricObjectPropertyAxiom owlAsymmetricObjectPropertyAxiom) {
            return owlAsymmetricObjectPropertyAxiom;
        }

        public OWLAxiom visit(OWLReflexiveObjectPropertyAxiom owlReflexiveObjectPropertyAxiom) {
            return owlReflexiveObjectPropertyAxiom;
        }

        public OWLAxiom visit(OWLDisjointClassesAxiom owlDisjointClassesAxiom) {
            return dataFactory.getOWLDisjointClassesAxiom(unfold(owlDisjointClassesAxiom.getClassExpressions(), Polarity.POSITIVE));
        }

        public OWLAxiom visit(OWLDataPropertyDomainAxiom owlDataPropertyDomainAxiom) {
            return dataFactory.getOWLDataPropertyDomainAxiom(owlDataPropertyDomainAxiom.getProperty(), owlDataPropertyDomainAxiom.getDomain());
        }

        public OWLAxiom visit(OWLObjectPropertyDomainAxiom owlObjectPropertyDomainAxiom) {
            return dataFactory.getOWLObjectPropertyDomainAxiom(owlObjectPropertyDomainAxiom.getProperty(), owlObjectPropertyDomainAxiom.getDomain());
        }

        public OWLAxiom visit(OWLEquivalentObjectPropertiesAxiom owlEquivalentObjectPropertiesAxiom) {
            return owlEquivalentObjectPropertiesAxiom;
        }

        public OWLAxiom visit(OWLNegativeDataPropertyAssertionAxiom owlNegativeDataPropertyAssertionAxiom) {
            return owlNegativeDataPropertyAssertionAxiom;
        }

        public OWLAxiom visit(OWLDifferentIndividualsAxiom owlDifferentIndividualsAxiom) {
            return owlDifferentIndividualsAxiom;
        }

        public OWLAxiom visit(OWLDisjointDataPropertiesAxiom owlDisjointDataPropertiesAxiom) {
            return owlDisjointDataPropertiesAxiom;
        }

        public OWLAxiom visit(OWLDisjointObjectPropertiesAxiom owlDisjointObjectPropertiesAxiom) {
            return owlDisjointObjectPropertiesAxiom;
        }

        public OWLAxiom visit(OWLObjectPropertyRangeAxiom owlObjectPropertyRangeAxiom) {
            return dataFactory.getOWLObjectPropertyRangeAxiom(owlObjectPropertyRangeAxiom.getProperty(), unfold(owlObjectPropertyRangeAxiom.getRange(), Polarity.POSITIVE));
        }

        public OWLAxiom visit(OWLObjectPropertyAssertionAxiom owlObjectPropertyAssertionAxiom) {
            return owlObjectPropertyAssertionAxiom;
        }

        public OWLAxiom visit(OWLFunctionalObjectPropertyAxiom owlFunctionalObjectPropertyAxiom) {
            return owlFunctionalObjectPropertyAxiom;
        }

        public OWLAxiom visit(OWLSubObjectPropertyOfAxiom owlSubObjectPropertyOfAxiom) {
            return owlSubObjectPropertyOfAxiom;
        }

        public OWLAxiom visit(OWLDisjointUnionAxiom owlDisjointUnionAxiom) {
            return owlDisjointUnionAxiom;
        }

        public OWLAxiom visit(OWLDeclarationAxiom owlDeclarationAxiom) {
            return owlDeclarationAxiom;
        }

        public OWLAxiom visit(OWLAnnotationAssertionAxiom owlAnnotationAssertionAxiom) {
            return owlAnnotationAssertionAxiom;
        }

        public OWLAxiom visit(OWLSymmetricObjectPropertyAxiom owlSymmetricObjectPropertyAxiom) {
            return owlSymmetricObjectPropertyAxiom;
        }

        public OWLAxiom visit(OWLDataPropertyRangeAxiom owlDataPropertyRangeAxiom) {
            return owlDataPropertyRangeAxiom;
        }

        public OWLAxiom visit(OWLFunctionalDataPropertyAxiom owlFunctionalDataPropertyAxiom) {
            return owlFunctionalDataPropertyAxiom;
        }

        public OWLAxiom visit(OWLEquivalentDataPropertiesAxiom owlEquivalentDataPropertiesAxiom) {
            return owlEquivalentDataPropertiesAxiom;
        }

        public OWLAxiom visit(OWLClassAssertionAxiom owlClassAssertionAxiom) {
            return dataFactory.getOWLClassAssertionAxiom(unfold(owlClassAssertionAxiom.getClassExpression(), Polarity.POSITIVE), owlClassAssertionAxiom.getIndividual());
        }

        public OWLAxiom visit(OWLEquivalentClassesAxiom owlEquivalentClassesAxiom) {
            return dataFactory.getOWLEquivalentClassesAxiom(unfold(owlEquivalentClassesAxiom.getClassExpressions(), Polarity.POSITIVE));
        }

        public OWLAxiom visit(OWLDataPropertyAssertionAxiom owlDataPropertyAssertionAxiom) {
            return owlDataPropertyAssertionAxiom;
        }

        public OWLAxiom visit(OWLTransitiveObjectPropertyAxiom owlTransitiveObjectPropertyAxiom) {
            return owlTransitiveObjectPropertyAxiom;
        }

        public OWLAxiom visit(OWLIrreflexiveObjectPropertyAxiom owlIrreflexiveObjectPropertyAxiom) {
            return owlIrreflexiveObjectPropertyAxiom;
        }

        public OWLAxiom visit(OWLSubDataPropertyOfAxiom owlSubDataPropertyOfAxiom) {
            return owlSubDataPropertyOfAxiom;
        }

        public OWLAxiom visit(OWLInverseFunctionalObjectPropertyAxiom owlInverseFunctionalObjectPropertyAxiom) {
            return owlInverseFunctionalObjectPropertyAxiom;
        }

        public OWLAxiom visit(OWLSameIndividualAxiom owlSameIndividualAxiom) {
            return owlSameIndividualAxiom;
        }

        public OWLAxiom visit(OWLSubPropertyChainOfAxiom owlSubPropertyChainOfAxiom) {
            return owlSubPropertyChainOfAxiom;
        }

        public OWLAxiom visit(OWLInverseObjectPropertiesAxiom owlInverseObjectPropertiesAxiom) {
            return owlInverseObjectPropertiesAxiom;
        }

        public OWLAxiom visit(OWLHasKeyAxiom owlHasKeyAxiom) {
            return owlHasKeyAxiom;
        }

        public OWLAxiom visit(OWLDatatypeDefinitionAxiom owlDatatypeDefinitionAxiom) {
            return owlDatatypeDefinitionAxiom;
        }

        public OWLAxiom visit(SWRLRule swrlRule) {
            return swrlRule;
        }

        public OWLAxiom visit(OWLSubAnnotationPropertyOfAxiom owlSubAnnotationPropertyOfAxiom) {
            return owlSubAnnotationPropertyOfAxiom;
        }

        public OWLAxiom visit(OWLAnnotationPropertyDomainAxiom owlAnnotationPropertyDomainAxiom) {
            return owlAnnotationPropertyDomainAxiom;
        }

        public OWLAxiom visit(OWLAnnotationPropertyRangeAxiom owlAnnotationPropertyRangeAxiom) {
            return owlAnnotationPropertyRangeAxiom;
        }
    }

    private OWLClassExpression getNamedClassExpression(Polarity pol, OWLClass namingClass) {
        if(pol.isPositive()) {
            Set<OWLClassExpression> ops = posName2ClassExpressionMap.get(namingClass);
            if(ops != null) {
                if(ops.size() > 1) {
                    return dataFactory.getOWLObjectIntersectionOf(ops);
                }
                else {
                    return ops.iterator().next();
                }
            }
        }
        else {
            Set<OWLClassExpression> ops = negName2ClassExpressionMap.get(namingClass);
            if(ops != null) {
                if(ops.size() > 1) {
                    return dataFactory.getOWLObjectUnionOf(ops);
                }
                else {
                    return ops.iterator().next();
                }
            }
        }
        return null;
    }

    private class ClassExpressionUnfolder implements OWLClassExpressionVisitorEx<OWLClassExpression> {

        private Polarity currentPolarity = Polarity.POSITIVE;

        private ClassExpressionUnfolder(Polarity currentPolarity) {
            this.currentPolarity = currentPolarity;
        }

        public OWLClassExpression visit(OWLClass owlClass) {
            OWLClassExpression namedExpression = getNamedClassExpression(currentPolarity, owlClass);
            if(namedExpression != null) {
                if(namedExpression.isAnonymous()) {
                    return namedExpression.accept(this);
                }
                else {
                    return namedExpression;
                }
            }
            else {
                return owlClass;
            }
        }

        private Set<OWLClassExpression> getUnfoldedExpressions(Set<OWLClassExpression> classExpressionSet) {
            Set<OWLClassExpression> unfolded = new HashSet<OWLClassExpression>();
            for(OWLClassExpression ce : classExpressionSet) {
                unfolded.add(ce.accept(this));
            }
            return unfolded;
        }


        public OWLClassExpression visit(OWLObjectIntersectionOf owlObjectIntersectionOf) {
            return dataFactory.getOWLObjectIntersectionOf(getUnfoldedExpressions(owlObjectIntersectionOf.getOperands()));
        }

        public OWLClassExpression visit(OWLObjectUnionOf owlObjectUnionOf) {
            return dataFactory.getOWLObjectUnionOf(getUnfoldedExpressions(owlObjectUnionOf.getOperands()));
        }

        public OWLClassExpression visit(OWLObjectComplementOf owlObjectComplementOf) {
            currentPolarity = currentPolarity.getReversePolarity();
            OWLClassExpression op = owlObjectComplementOf.getOperand().accept(this);
            currentPolarity = currentPolarity.getReversePolarity();
            return dataFactory.getOWLObjectComplementOf(op);
        }

        public OWLClassExpression visit(OWLObjectSomeValuesFrom owlObjectSomeValuesFrom) {
            return dataFactory.getOWLObjectSomeValuesFrom(owlObjectSomeValuesFrom.getProperty(), owlObjectSomeValuesFrom.getFiller().accept(this));
        }

        public OWLClassExpression visit(OWLObjectAllValuesFrom owlObjectAllValuesFrom) {
            return dataFactory.getOWLObjectAllValuesFrom(owlObjectAllValuesFrom.getProperty(), owlObjectAllValuesFrom.getFiller().accept(this));
        }

        public OWLClassExpression visit(OWLObjectHasValue owlObjectHasValue) {
            return owlObjectHasValue;
        }

        public OWLClassExpression visit(OWLObjectMinCardinality owlObjectMinCardinality) {
            return dataFactory.getOWLObjectMinCardinality(owlObjectMinCardinality.getCardinality(), owlObjectMinCardinality.getProperty(), owlObjectMinCardinality.getFiller().accept(this));
        }

        public OWLClassExpression visit(OWLObjectExactCardinality owlObjectExactCardinality) {
            return dataFactory.getOWLObjectExactCardinality(owlObjectExactCardinality.getCardinality(), owlObjectExactCardinality.getProperty(), owlObjectExactCardinality.getFiller().accept(this));
        }

        public OWLClassExpression visit(OWLObjectMaxCardinality owlObjectMaxCardinality) {
            currentPolarity = currentPolarity.getReversePolarity();
            OWLClassExpression filler = owlObjectMaxCardinality.getFiller().accept(this);
            currentPolarity = currentPolarity.getReversePolarity();
            return dataFactory.getOWLObjectMaxCardinality(owlObjectMaxCardinality.getCardinality(), owlObjectMaxCardinality.getProperty(), filler);
        }

        public OWLClassExpression visit(OWLObjectHasSelf owlObjectHasSelf) {
            return owlObjectHasSelf;
        }

        public OWLClassExpression visit(OWLObjectOneOf owlObjectOneOf) {
            return owlObjectOneOf;
        }

        public OWLClassExpression visit(OWLDataSomeValuesFrom owlDataSomeValuesFrom) {
            return owlDataSomeValuesFrom;
        }

        public OWLClassExpression visit(OWLDataAllValuesFrom owlDataAllValuesFrom) {
            return owlDataAllValuesFrom;
        }

        public OWLClassExpression visit(OWLDataHasValue owlDataHasValue) {
            return owlDataHasValue;
        }

        public OWLClassExpression visit(OWLDataMinCardinality owlDataMinCardinality) {
            return owlDataMinCardinality;
        }

        public OWLClassExpression visit(OWLDataExactCardinality owlDataExactCardinality) {
            return owlDataExactCardinality;
        }

        public OWLClassExpression visit(OWLDataMaxCardinality owlDataMaxCardinality) {
            return owlDataMaxCardinality;
        }
    }
}
