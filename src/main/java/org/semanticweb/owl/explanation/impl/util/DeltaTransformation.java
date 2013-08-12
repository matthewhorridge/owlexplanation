package org.semanticweb.owl.explanation.impl.util;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;

import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.*;
/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 14/02/2011
 */
public class DeltaTransformation implements AxiomTransformation {

    private int freshIRICounter = 0;

    private OWLDataFactory dataFactory;

    private Set<OWLEntity> freshEntities = new HashSet<OWLEntity>();

    private Set<OWLAxiom> transformedAxioms = new HashSet<OWLAxiom>();

//    private OWLAnnotation currentAxiomAnnotation;

    private int currentAxiomCount = 0;

    private Map<OWLAxiom, Integer> namingAxiom2ModalDepth = new HashMap<OWLAxiom, Integer>();

    private int modalDepth = 0;

    public DeltaTransformation(OWLDataFactory dataFactory) {
        this.dataFactory = dataFactory;
    }

    private boolean isFreshEntity(OWLEntity entity) {
        return freshEntities.contains(entity);
    }

    private OWLClass getFreshClass() {
        OWLClass freshClass = dataFactory.getOWLClass(getNextFreshIRI());
        freshEntities.add(freshClass);
        return freshClass;
    }

    private OWLDatatype getFreshDatatype() {
        OWLDatatype freshDatatype = dataFactory.getOWLDatatype(getNextFreshIRI());
        freshEntities.add(freshDatatype);
        return freshDatatype;
    }

    private OWLNamedIndividual getFreshIndividual() {
        OWLNamedIndividual freshIndividual = dataFactory.getOWLNamedIndividual(getNextFreshIRI());
        freshEntities.add(freshIndividual);
        return freshIndividual;
    }

    private IRI getNextFreshIRI() {
        freshIRICounter++;
        return IRI.create("http://owlapi.sourceforge.net/transform/flattening#X" + freshIRICounter);
    }

    public Set<OWLAxiom> transform(Set<OWLAxiom> axioms) {
        transformedAxioms.clear();
        namingAxiom2ModalDepth.clear();
        AxiomTransformer transformer = new AxiomTransformer();
        for (OWLAxiom ax : axioms) {
            currentAxiomCount++;
//            currentAxiomAnnotation = getCurrentAxiomAnnotation();
            transformedAxioms.addAll(ax.accept(transformer));
        }
        return transformedAxioms;
    }

    public int getModalDepth(OWLAxiom renamingAxiom) {
        Integer depth = namingAxiom2ModalDepth.get(renamingAxiom);
        if(depth == null) {
            return 0;
        }
        else {
            return depth;
        }
    }


//    private OWLAnnotation getCurrentAxiomAnnotation() {
//        OWLLiteral annoValue = dataFactory.getOWLLiteral(currentAxiomCount);
//        IRI iri = IRI.create("http://owl.cs.manchester.ac.uk/explanation/vocabulary#axiomId");
//        OWLAnnotationProperty property = dataFactory.getOWLAnnotationProperty(iri);
//        return dataFactory.getOWLAnnotation(property, annoValue);
//    }

    private boolean isFreshClass(OWLClassExpression ce) {
        return !ce.isAnonymous() && freshEntities.contains(ce.asOWLClass());
    }

//    private boolean isFreshDataRange(OWLDataRange dr) {
//        return dr.isDatatype() && freshEntities.contains(dr.asOWLDatatype());
//    }

    private OWLNamedIndividual assignName(OWLIndividual individual) {
        OWLNamedIndividual freshIndividual = getFreshIndividual();
        Set<OWLIndividual> individuals = new HashSet<OWLIndividual>();
        individuals.add(individual);
        individuals.add(freshIndividual);
//        Set<OWLAnnotation> axiomId = Collections.singleton(currentAxiomAnnotation);
//        OWLSameIndividualAxiom namingAxiom = dataFactory.getOWLSameIndividualAxiom(individuals, axiomId);
        OWLSameIndividualAxiom namingAxiom = dataFactory.getOWLSameIndividualAxiom(individuals);
        namingAxiom2ModalDepth.put(namingAxiom, modalDepth);
        transformedAxioms.add(namingAxiom);
        return freshIndividual;
    }

    private OWLClass assignName(OWLClassExpression classExpression, Polarity polarity) {
        if(polarity.isPositive()) {
            if(classExpression.isOWLThing()) {
                return classExpression.asOWLClass();
            }
        }
        else {
            if(classExpression.isOWLNothing()) {
                return classExpression.asOWLClass();
            }
        }
        if(isFreshClass(classExpression)) {
            return classExpression.asOWLClass();
        }
        OWLClass freshClass = getFreshClass();
        return assignName(classExpression, polarity, freshClass);
    }

//    private OWLDatatype assignName(OWLDataRange dr, Polarity polarity) {
//        if(polarity.isPositive()) {
//            if(dr.isTopDatatype()) {
//                return dr.asOWLDatatype();
//            }
//        }
//        if(isFreshDataRange(dr)) {
//            return dr.asOWLDatatype();
//        }
//        OWLDatatype freshDatatype = getFreshDatatype();
//        return assignName(dr, polarity, freshDatatype);
//    }
//
//    private OWLDatatype assignName(OWLDataRange dr, Polarity polarity, OWLDatatype freshDatatype) {
//        OWLDatatypeDefinitionAxiom namingAxiom;
//        Set<OWLAnnotation> axiomId = Collections.emptySet();
//        if (polarity.isPositive()) {
//            namingAxiom = dataFactory.getOWLDatatypeDefinitionAxiom(freshDatatype, dr);
//        }
//        else {
//            namingAxiom = dataFactory.getOWLDatatypeDefinitionAxiom(freshDatatype, dr);
//        }
//        namingAxiom2ModalDepth.put(namingAxiom, modalDepth);
//        transformedAxioms.add(namingAxiom);
//        return freshDatatype;
//    }

    private OWLClass assignName(OWLClassExpression classExpression, Polarity polarity, OWLClass freshClass) {
        OWLSubClassOfAxiom namingAxiom;
        Set<OWLAnnotation> axiomId = Collections.emptySet();
        if (polarity.isPositive()) {
            namingAxiom = dataFactory.getOWLSubClassOfAxiom(freshClass, classExpression, axiomId);
        }
        else {
            namingAxiom = dataFactory.getOWLSubClassOfAxiom(classExpression, freshClass, axiomId);
        }
        namingAxiom2ModalDepth.put(namingAxiom, modalDepth);
        transformedAxioms.add(namingAxiom);
        return freshClass;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    private class AxiomTransformer implements OWLAxiomVisitorEx<Set<OWLAxiom>> {

        private ClassExpressionTransformer positiveTransformer = new ClassExpressionTransformer(Polarity.POSITIVE);

        private ClassExpressionTransformer negativeTransformer = new ClassExpressionTransformer(Polarity.NEGATIVE);

        private Set<OWLAxiom> visit(Set<? extends OWLAxiom> axioms) {
            Set<OWLAxiom> result = new HashSet<OWLAxiom>();
            for (OWLAxiom ax : axioms) {
                result.addAll(ax.accept(this));
            }
            return result;
        }

        public Set<OWLAxiom> visit(OWLSubClassOfAxiom axiom) {
//            if(!axiom.getSubClass().isAnonymous() && !axiom.getSuperClass().isAnonymous()) {
//                return Collections.<OWLAxiom>singleton(axiom);
//            }
            OWLClass freshSub;
//            if (axiom.getSubClass().isAnonymous()) {
                OWLClassExpression subClass = axiom.getSubClass().accept(negativeTransformer);
                freshSub = assignName(subClass, Polarity.NEGATIVE);
//            }
//            else {
//                freshSub = axiom.getSubClass().asOWLClass();
//            }

            OWLClass freshSuper;
//            if (axiom.getSuperClass().isAnonymous()) {
                OWLClassExpression superClass = axiom.getSuperClass().accept(positiveTransformer);
                freshSuper = assignName(superClass, Polarity.POSITIVE);
//            }
//            else {
//                freshSuper = axiom.getSuperClass().asOWLClass();
//            }

            Set<OWLAxiom> result = new HashSet<OWLAxiom>();
            result.add(dataFactory.getOWLSubClassOfAxiom(freshSub, freshSuper));
            return result;

        }

        public Set<OWLAxiom> visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
            return axiom.asOWLSubClassOfAxiom().accept(this);
        }

        public Set<OWLAxiom> visit(OWLAsymmetricObjectPropertyAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLReflexiveObjectPropertyAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLDisjointClassesAxiom axiom) {
            Set<OWLAxiom> result = new HashSet<OWLAxiom>();
            // TODO: FIX!
//            for(OWLAxiom ax : axiom.asOWLSubClassOfAxioms()) {
//                result.addAll(ax.accept(this));
//            }
            return Collections.<OWLAxiom>singleton(axiom);
//            if (axiom.getClassExpressions().size() > 2) {
//                return visit(axiom.asPairwiseAxioms());
//            }
//            else if(axiom.getClassExpressions().size() == 2) {
//                List<OWLClassExpression> ops = new ArrayList<OWLClassExpression>(axiom.getClassExpressions());
//                OWLClassExpression first = assignName(ops.get(0), Polarity.POSITIVE);
//                OWLClassExpression second = assignName(ops.get(1), Polarity.NEGATIVE);
//                return Collections.<OWLAxiom>singleton(dataFactory.getOWLSubClassOfAxiom(first, second));
//            }
//            else {
//                return Collections.<OWLAxiom>singleton(axiom);
//            }
        }

        public Set<OWLAxiom> visit(OWLDataPropertyDomainAxiom axiom) {
            return axiom.asOWLSubClassOfAxiom().accept(this);
        }

        public Set<OWLAxiom> visit(OWLObjectPropertyDomainAxiom axiom) {
            OWLClassExpression renamedDomain = axiom.getDomain().accept(positiveTransformer);
            OWLAxiom transformed = dataFactory.getOWLObjectPropertyDomainAxiom(axiom.getProperty(), renamedDomain);
            return Collections.singleton(transformed);
        }

        public Set<OWLAxiom> visit(OWLEquivalentObjectPropertiesAxiom axiom) {
            return visit(axiom.asSubObjectPropertyOfAxioms());
        }

        public Set<OWLAxiom> visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
            return axiom.asOWLSubClassOfAxiom().accept(this);
        }

        public Set<OWLAxiom> visit(OWLDifferentIndividualsAxiom axiom) {
            Set<OWLIndividual> renamed = new HashSet<OWLIndividual>();
            for(OWLIndividual ind : axiom.getIndividuals()) {
                renamed.add(assignName(ind));
            }
            return Collections.<OWLAxiom>singleton(dataFactory.getOWLDifferentIndividualsAxiom(renamed));
        }

        public Set<OWLAxiom> visit(OWLDisjointDataPropertiesAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLDisjointObjectPropertiesAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLObjectPropertyRangeAxiom axiom) {
            modalDepth++;
            OWLClassExpression renamedRange = axiom.getRange().accept(positiveTransformer);
            modalDepth--;
            OWLAxiom transformed = dataFactory.getOWLObjectPropertyRangeAxiom(axiom.getProperty(), renamedRange);
            return Collections.singleton(transformed);
        }

        public Set<OWLAxiom> visit(OWLObjectPropertyAssertionAxiom axiom) {
            OWLNamedIndividual renamedSubject = assignName(axiom.getSubject());
            OWLNamedIndividual renamedObject = assignName(axiom.getObject());
            OWLObjectPropertyAssertionAxiom flattendAx = dataFactory.getOWLObjectPropertyAssertionAxiom(axiom.getProperty(), renamedSubject, renamedObject);
            return Collections.<OWLAxiom>singleton(flattendAx);
        }

        public Set<OWLAxiom> visit(OWLFunctionalObjectPropertyAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLSubObjectPropertyOfAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLDisjointUnionAxiom axiom) {
            Set<OWLAxiom> result = new HashSet<OWLAxiom>();
            result.addAll(axiom.getOWLDisjointClassesAxiom().accept(this));
            result.addAll(axiom.getOWLEquivalentClassesAxiom().accept(this));
            return result;
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
            return visit(axiom.asSubDataPropertyOfAxioms());
        }

        public Set<OWLAxiom> visit(OWLClassAssertionAxiom axiom) {
            OWLClass renamedCls = assignName(axiom.getClassExpression(), Polarity.POSITIVE);
            OWLNamedIndividual renamedInd = assignName(axiom.getIndividual());
            OWLClassAssertionAxiom flattenedAx = dataFactory.getOWLClassAssertionAxiom(renamedCls, renamedInd);
            return Collections.<OWLAxiom>singleton(flattenedAx);
        }

        public Set<OWLAxiom> visit(OWLEquivalentClassesAxiom axiom) {
            return visit(axiom.asOWLSubClassOfAxioms());
        }

        public Set<OWLAxiom> visit(OWLDataPropertyAssertionAxiom axiom) {
            return axiom.asOWLSubClassOfAxiom().accept(this);
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
            Set<OWLNamedIndividual> renamed = new HashSet<OWLNamedIndividual>();
            for(OWLIndividual ind : axiom.getIndividuals()) {
                renamed.add(assignName(ind));
            }
            return Collections.<OWLAxiom>singleton(dataFactory.getOWLSameIndividualAxiom(renamed));

        }

        public Set<OWLAxiom> visit(OWLSubPropertyChainOfAxiom axiom) {
            return Collections.<OWLAxiom>singleton(axiom);
        }

        public Set<OWLAxiom> visit(OWLInverseObjectPropertiesAxiom axiom) {
            return visit(axiom.asSubObjectPropertyOfAxioms());
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

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    private class ClassExpressionTransformer implements OWLClassExpressionVisitorEx<OWLClassExpression>, OWLDataRangeVisitorEx<OWLDataRange> {

        private Polarity polarity;

        private ClassExpressionTransformer(Polarity polarity) {
            this.polarity = polarity;
        }

        private Set<OWLClassExpression> getRenamedClasses(Set<OWLClassExpression> classes, boolean useSameName) {
            Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
            if(useSameName) {
                OWLClass name = getFreshClass();
                for(OWLClassExpression ce : classes) {
                    if(polarity.isPositive()) {
                        if(!ce.isOWLThing()) {
                            OWLClassExpression ceP = ce.accept(this);
                            assignName(ceP, Polarity.POSITIVE, name);
                        }
                    }
                    else {
                        if(!ce.isOWLNothing()) {
                            OWLClassExpression ceP = ce.accept(this);
                            assignName(ceP, Polarity.NEGATIVE, name);
                        }
                    }
                }
                return Collections.<OWLClassExpression>singleton(name);
            }
            else {
                for (OWLClassExpression cls : classes) {
                    OWLClassExpression transCls = cls.accept(this);
                    OWLClassExpression renaming = assignName(transCls, polarity);
                    result.add(renaming);
                }
            }
            return result;
        }

        public OWLClass visit(OWLClass ce) {
            return ce;
        }

        public OWLClassExpression visit(OWLObjectIntersectionOf ce) {
            Set<OWLClassExpression> renamedOperands = getRenamedClasses(ce.getOperands(), polarity.isPositive());
            if (renamedOperands.size() == 1) {
                return renamedOperands.iterator().next();
            }
            else {
                return dataFactory.getOWLObjectIntersectionOf(renamedOperands);
            }
        }

        public OWLClassExpression visit(OWLObjectUnionOf ce) {
            Set<OWLClassExpression> renamedOperands = getRenamedClasses(ce.getOperands(), !polarity.isPositive());
            if(renamedOperands.size() == 1) {
                return renamedOperands.iterator().next();
            }
            else {
                return dataFactory.getOWLObjectUnionOf(renamedOperands);
            }
        }

        public OWLClassExpression visit(OWLObjectComplementOf ce) {
            polarity = polarity.getReversePolarity();
            OWLClassExpression renamedComplement = assignName(ce.getOperand().accept(this), polarity);
            polarity = polarity.getReversePolarity();
            return dataFactory.getOWLObjectComplementOf(renamedComplement);
        }

        public OWLClassExpression visit(OWLObjectSomeValuesFrom ce) {
            modalDepth++;
            OWLClassExpression renamedFiller = assignName(ce.getFiller().accept(this), polarity);
            modalDepth--;
            OWLObjectPropertyExpression property = ce.getProperty();
            return dataFactory.getOWLObjectSomeValuesFrom(property, renamedFiller);
        }

        public OWLClassExpression visit(OWLObjectAllValuesFrom ce) {
            modalDepth++;
            OWLClassExpression renamedFiller = assignName(ce.getFiller().accept(this), polarity);
            modalDepth--;
            OWLObjectPropertyExpression property = ce.getProperty();
            return dataFactory.getOWLObjectAllValuesFrom(property, renamedFiller);
        }

        public OWLClassExpression visit(OWLObjectHasValue ce) {
            modalDepth++;
            OWLNamedIndividual renamedInd = assignName(ce.getValue());
            modalDepth--;
            return dataFactory.getOWLObjectHasValue(ce.getProperty(), renamedInd);
        }

        public OWLClassExpression visit(OWLObjectMinCardinality ce) {
            modalDepth++;
            OWLClassExpression renamedFiller = assignName(ce.getFiller().accept(this), polarity);
            modalDepth--;
            OWLObjectPropertyExpression prop = ce.getProperty();
            int cardi = ce.getCardinality();
            return dataFactory.getOWLObjectMinCardinality(cardi, prop, renamedFiller);
        }

        public OWLClassExpression visit(OWLObjectExactCardinality ce) {
            return ce.asIntersectionOfMinMax().accept(this);
        }

        public OWLClassExpression visit(OWLObjectMaxCardinality ce) {
            polarity = polarity.getReversePolarity();
            modalDepth++;
            OWLClass renamedFiller = assignName(ce.getFiller().accept(this), polarity);
            modalDepth--;
            polarity = polarity.getReversePolarity();
            OWLObjectPropertyExpression prop = ce.getProperty();
            int cardi = ce.getCardinality();
            return dataFactory.getOWLObjectMaxCardinality(cardi, prop, renamedFiller);
        }

        public OWLClassExpression visit(OWLObjectHasSelf ce) {
            return ce;
        }

        public OWLClassExpression visit(OWLObjectOneOf ce) {
            Set<OWLNamedIndividual> renamed = new HashSet<OWLNamedIndividual>();
            for(OWLIndividual ind : ce.getIndividuals()) {
                renamed.add(assignName(ind));
            }
            return dataFactory.getOWLObjectOneOf(renamed);
        }

        public OWLClassExpression visit(OWLDataSomeValuesFrom ce) {
//            modalDepth++;
//            OWLDatatype renamedFiller = assignName(ce.getFiller().accept(this), polarity);
//            modalDepth--;
//            return dataFactory.getOWLDataSomeValuesFrom(ce.getProperty(), renamedFiller);
            return ce;
        }

        public OWLClassExpression visit(OWLDataAllValuesFrom ce) {
            return ce;
        }

        public OWLClassExpression visit(OWLDataHasValue ce) {
            return ce;
        }

        public OWLClassExpression visit(OWLDataMinCardinality ce) {
            return ce;
        }

        public OWLClassExpression visit(OWLDataExactCardinality ce) {
            return ce;
        }

        public OWLClassExpression visit(OWLDataMaxCardinality ce) {
            return ce;
        }

        public OWLDataRange visit(OWLDatatype node) {
            return node;
        }

        public OWLDataRange visit(OWLDataOneOf node) {
            return node;
        }

        public OWLDataRange visit(OWLDataComplementOf node) {
            return node;
        }

        public OWLDataRange visit(OWLDataIntersectionOf node) {
            return node;
        }

        public OWLDataRange visit(OWLDataUnionOf node) {
            return node;
        }

        public OWLDataRange visit(OWLDatatypeRestriction node) {
            return node;
        }
    }

    public static void main(String[] args) {
        DefaultPrefixManager pm = new DefaultPrefixManager("http://test.com#");
        OWLClass A = Class(":A", pm);
        OWLClass B = Class(":B", pm);
        OWLClass C = Class(":C", pm);
        OWLObjectProperty prop = ObjectProperty(":p", pm);
        OWLIndividual i = NamedIndividual(":i", pm);
        OWLIndividual j = NamedIndividual(":j", pm);
        OWLIndividual k = NamedIndividual(":k", pm);
        OWLIndividual l = NamedIndividual(":l", pm);
        OWLDataFactory df = new OWLDataFactoryImpl();
//        OWLAxiom ax = SubClassOf(A, ObjectIntersectionOf(B, ObjectIntersectionOf(ObjectComplementOf(B), C)));
//        OWLAxiom ax = SubClassOf(A, ObjectSomeValuesFrom(prop, OWLThing()));
//        OWLAxiom ax = SubClassOf(A, ObjectAllValuesFrom(prop, B));
        OWLAxiom ax = SubClassOf(A, ObjectOneOf(i, j, k, l));
//        ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
        System.out.println(ax);
        System.out.println("---------------------------------------------------");
        DeltaTransformation transformation = new DeltaTransformation(df);
        for(OWLAxiom axt : transformation.transform(Collections.singleton(ax))) {
            System.out.println(axt);
        }


    }

}
