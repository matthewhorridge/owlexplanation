package org.semanticweb.owl.explanation.impl.laconic;

import org.semanticweb.owl.explanation.impl.util.AxiomTransformation;
import org.semanticweb.owlapi.model.*;

import java.util.*;
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
 * 15-Sep-2008<br><br>
 */
public class OPlusGenerator implements OWLAxiomVisitorEx<Set<? extends OWLAxiom>>, AxiomTransformation {

    private OWLDataFactory dataFactory;

    private BetaGenerator betaGenerator;

    private TauGenerator tauGenerator;

    private TriviallyBottomChecker bottomChecker;

    private TriviallyTopChecker topChecker;

    private Map<OWLAxiom, Set<OWLAxiom>> axiom2SourceMap = new HashMap<OWLAxiom, Set<OWLAxiom>>();

    private OPlusSplitting splitting;

    private OWLAxiom currentSourceAxiom;

    public OPlusGenerator(OWLDataFactory dataFactory, OPlusSplitting splitting) {
        this.dataFactory = dataFactory;
        this.splitting = splitting;
        betaGenerator = new BetaGenerator(dataFactory);
        tauGenerator = new TauGenerator(dataFactory);
        bottomChecker = new TriviallyBottomChecker();
        topChecker = new TriviallyTopChecker();
    }

    public Set<OWLAxiom> transform(Set<OWLAxiom> axioms) {
        Set<OWLAxiom> result = new HashSet<OWLAxiom>();
        int count = 0;
        for(OWLAxiom ax : axioms) {
            count++;
//            System.out.print(count);
//            System.out.print("   ");
//            System.out.println(ax);
            currentSourceAxiom = ax;
                Set<? extends OWLAxiom> axiomResult = ax.accept(this);
//            for(OWLAxiom a : axiomResult) {
//                System.out.println("\t\t\t" + a);
//            }

                result.addAll(axiomResult);
//            System.out.println("----   " + axiomResult.size());
        }
//        System.out.println("Result size: " + result.size());
        return result;
    }

    public boolean isNothing(OWLClassExpression desc) {
        return desc.accept(bottomChecker);
    }

    public boolean isThing(OWLClassExpression desc) {
        return desc.accept(topChecker);
    }

    public void reset() {
        axiom2SourceMap.clear();
    }

    public Map<OWLAxiom, Set<OWLAxiom>> getAxiom2SourceMap() {
        return axiom2SourceMap;
    }

    public boolean hasSameSource(OWLAxiom ax1, OWLAxiom ax2) {
        Set<OWLAxiom> source1 = axiom2SourceMap.get(ax1);
        Set<OWLAxiom> source2 = axiom2SourceMap.get(ax2);
        if(source1 == null || source2 == null) {
            return false;
        }
        for(OWLAxiom source1Ax : source1) {
            if(source2.contains(source1Ax)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasMultipleSources(OWLAxiom axiom) {
        Set<OWLAxiom> sources = axiom2SourceMap.get(axiom);
        return sources != null && sources.size() > 1;
    }

    /**
     * Gets axioms that have the same source axioms as the specified axiom.
     * @param axiom The axiom for which axioms with the same sources will be retrieved.
     * @param toSearch The set of axioms to seach for the same sources.  If <code>toSearch</code>
     * contains <code>axiom</code> then <code>axiom</code> will simply be ignored.
     * @return The axioms that have the same sources as <code>axiom</code>.  This set will not contain
     * <code>axiom</code>.
     */
    public Set<OWLAxiom> getSameSourceAxioms(OWLAxiom axiom, Set<OWLAxiom> toSearch) {
        Set<OWLAxiom> axiomSources = axiom2SourceMap.get(axiom);
        if(axiomSources == null) {
            return Collections.emptySet();
        }
        Set<OWLAxiom> result = new HashSet<OWLAxiom>();
        for(OWLAxiom ax : toSearch) {
            Set<OWLAxiom> sources = axiom2SourceMap.get(ax);
            if(sources != null) {
                for(OWLAxiom axiomSourceAxiom : axiomSources) {
                    if(sources.contains(axiomSourceAxiom)) {
                        result.add(ax);
                        break;
                    }
                }
            }
        }
        return result;
    }

    public void addSources(OWLAxiom ax, Set<OWLAxiom> sources) {
        Set<OWLAxiom> existingSources = axiom2SourceMap.get(ax);
        if (existingSources == null) {
            existingSources = new HashSet<OWLAxiom>();
            axiom2SourceMap.put(ax, existingSources);
        }
        existingSources.addAll(sources);
    }

    public Set<OWLAxiom> getSources(OWLAxiom axiom) {
        Set<OWLAxiom> sources = axiom2SourceMap.get(axiom);
        if(sources == null) {
            sources = Collections.emptySet();
        }
        return sources;
    }

    public Set<OWLAxiom> log(OWLAxiom source, Set<OWLAxiom> axioms) {
//        getSourceAxiomAnnotation(source);
//        Set<OWLAxiom> annotatedAxioms = new HashSet<OWLAxiom>();
//        for (OWLAxiom ax : axioms) {
//            OWLAxiom annotated = ax.getAnnotatedAxiom(Collections.singleton(sourceAnnotation));
//            annotatedAxioms.add(annotated);
//        }
//        return annotatedAxioms;
        //--------------------------------------------
        for(OWLAxiom ax : axioms) {
            Set<OWLAxiom> sourceAxioms = axiom2SourceMap.get(ax);
            if(sourceAxioms == null) {
                sourceAxioms = new HashSet<OWLAxiom>(4);
                axiom2SourceMap.put(ax, sourceAxioms);
            }
            sourceAxioms.add(currentSourceAxiom);
        }
        return axioms;
    }

    private OWLAnnotation getSourceAxiomAnnotation(OWLAxiom source) {
        OWLAnnotationProperty sourceAnnotationProperty = dataFactory.getOWLAnnotationProperty(OPlusSplitting.SOURCE_AXIOM_IRI);
        OWLLiteral identityLiteral = dataFactory.getOWLLiteral(System.identityHashCode(source));
        return dataFactory.getOWLAnnotation(sourceAnnotationProperty, identityLiteral);
    }

    public Set<OWLAxiom> log(OWLAxiom source) {
        return log(source, Collections.singleton(source));
    }

    public Set<OWLAxiom> visit(OWLSubClassOfAxiom axiom) {
        // Make the LHS smaller and the RHS larger
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();


        // Optimisation
        Set<OWLClassExpression> tau = new HashSet<OWLClassExpression>();
        Set<OWLClassExpression> beta = new HashSet<OWLClassExpression>();

        Set<OWLAnnotation> sourceAnnotations;
        if(splitting.equals(OPlusSplitting.TOP_LEVEL)) {
            Set<OWLClassExpression> superClassConjuncts = axiom.getSuperClass().asConjunctSet();
            for(OWLClassExpression superClassConjunct : superClassConjuncts) {
                tau.addAll(superClassConjunct.accept(tauGenerator));
            }

            OWLClassExpression subClass = axiom.getSubClass();
            Set<OWLClassExpression> subClassDisjuncts;
            if(subClass instanceof OWLObjectOneOf) {
                OWLObjectOneOf objectOneOf = (OWLObjectOneOf) subClass;
                subClassDisjuncts = objectOneOf.asObjectUnionOf().asDisjunctSet();
            }
            else {
                subClassDisjuncts = subClass.asDisjunctSet();
            }
            for(OWLClassExpression subClassDisjunct : subClassDisjuncts) {
//                beta.addAll(Collections.singleton(subClassDisjunct));
                beta.addAll(subClassDisjunct.accept(betaGenerator));
            }
            sourceAnnotations = Collections.emptySet();// Collections.singleton(getSourceAxiomAnnotation(axiom));
        }
        else {
            tau.addAll(axiom.getSuperClass().accept(tauGenerator));
            beta.addAll(axiom.getSubClass().accept(betaGenerator));
            sourceAnnotations = Collections.emptySet();
        }

//            if (axiom.getSuperClass() instanceof OWLObjectUnionOf) {
//                boolean allNamed = true;
//                for (OWLClassExpression op : (((OWLObjectUnionOf) axiom.getSuperClass()).getOperands())) {
//                    if (op.isAnonymous()) {
//                        allNamed = false;
//                        break;
//                    }
//                }
//                if (allNamed) {
//                    tau.add(axiom.getSuperClass());
//                }
//                else {
//                    tau = axiom.getSuperClass().accept(tauGenerator);
//                }
//            }
//            else {
//                tau = axiom.getSuperClass().accept(tauGenerator);
//            }
//


        // Generate all possible LHS and RHS concepts and pair them up
        for (OWLClassExpression tauDesc : tau) {
            if (!isThing(tauDesc)) {
                for (OWLClassExpression betaDesc : beta) {
//                    if (!isNothing(betaDesc) && !(tauDesc instanceof OWLObjectIntersectionOf) && !(betaDesc instanceof OWLObjectUnionOf)) {
                    if (!isNothing(betaDesc) && !isThing(tauDesc)) {
                        OWLSubClassOfAxiom ax = dataFactory.getOWLSubClassOfAxiom(betaDesc, tauDesc, sourceAnnotations);
                        axioms.add(ax);
                    }
//                    }
//                    else {
//                        System.out.println("IGNORING: " + betaDesc + " -> XXXX");
//                    }
                }
            }
            else {
//                System.out.println("IGNORING: XXXX -> " + tauDesc);
            }
        }
        return log(axiom, axioms);
    }


    public Set<? extends OWLAxiom> visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLReflexiveObjectPropertyAxiom axiom) {
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLDisjointClassesAxiom axiom) {
        boolean containAnonDescriptions = false;
        for (OWLClassExpression desc : axiom.getClassExpressions()) {
            if (desc.isAnonymous()) {
                containAnonDescriptions = true;
                break;
            }
        }
        if (!containAnonDescriptions) {
            return log(axiom);
        }
        // We treat DisjointClasses(C, D) as syntactic sugar for
        // SubClassOf(C, ObjectComplementOf(D)).
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        List<OWLClassExpression> descs = new ArrayList<OWLClassExpression>(axiom.getClassExpressions());
        for (int i = 0; i < descs.size(); i++) {
            for (int j = i + 1; j < descs.size(); j++) {
                Set<? extends OWLAxiom> weakendAxioms = dataFactory.getOWLSubClassOfAxiom(descs.get(i), dataFactory.getOWLObjectComplementOf(descs.get(j))).accept(this);
                for (OWLAxiom ax : weakendAxioms) {
                    if (ax.isOfType(AxiomType.SUBCLASS_OF)) {
                        OWLSubClassOfAxiom sca = (OWLSubClassOfAxiom) ax;
                        if (sca.getSubClass() instanceof OWLObjectComplementOf) {
                            OWLClassExpression clsA = ((OWLObjectComplementOf) sca.getSubClass()).getOperand();
                            OWLClassExpression clsB = sca.getSuperClass();
                            axioms.add(dataFactory.getOWLDisjointClassesAxiom(clsA, clsB));
                        }
                        else if (sca.getSuperClass() instanceof OWLObjectComplementOf) {
                            OWLClassExpression clsA = sca.getSubClass();
                            OWLClassExpression clsB = ((OWLObjectComplementOf) sca.getSuperClass()).getOperand();
                            axioms.add(dataFactory.getOWLDisjointClassesAxiom(clsA, clsB));
                        }
                        else {
                            axioms.add(ax);
                        }
                    }
                    else {
                        axioms.add(ax);
                    }
                }
            }
        }
        return log(axiom, axioms);
    }


    public Set<? extends OWLAxiom> visit(OWLDataPropertyDomainAxiom axiom) {
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLObjectPropertyDomainAxiom axiom) {
        // A domain checker, Domain(R, C) is syntactic sugar for
        // R some Thing -> C.  Therefore, we just weaken the domain using
        // a tau transform
        Set<OWLAxiom> result = new HashSet<OWLAxiom>();
        for (OWLClassExpression dom : axiom.getDomain().accept(tauGenerator)) {
            if (!isThing(dom)) {
                result.add(dataFactory.getOWLObjectPropertyDomainAxiom(axiom.getProperty(), dom));
            }
        }
        return log(axiom, result);
    }


    public Set<? extends OWLAxiom> visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        // Bi-implications
        Set<OWLAxiom> result = new HashSet<OWLAxiom>();
        for (OWLObjectPropertyExpression propA : axiom.getProperties()) {
            for (OWLObjectPropertyExpression propB : axiom.getProperties()) {
                if (!propA.equals(propB)) {
                    result.add(dataFactory.getOWLSubObjectPropertyOfAxiom(propA, propB));
                }
            }
        }
        return log(axiom, result);
    }


    public Set<? extends OWLAxiom> visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLDifferentIndividualsAxiom axiom) {
        // Pairwise, or the power set?  It should be pairwise - an optimisation.
        // This means if we have something like  Diff(a, b, c, d) and we only
        // care that Diff(a, b) and Diff(a, c) then we can strike out d
        // from the original checker. In otherwords, we only need the weakest sets
        Set<OWLAxiom> result = new HashSet<OWLAxiom>();
        for (OWLIndividual indA : axiom.getIndividuals()) {
            for (OWLIndividual indB : axiom.getIndividuals()) {
                if (!indA.equals(indB)) {
                    result.add(dataFactory.getOWLDifferentIndividualsAxiom(indA, indB));
                }
            }
        }
        return log(axiom, result);
    }


    public Set<? extends OWLAxiom> visit(OWLDisjointDataPropertiesAxiom axiom) {
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLDisjointObjectPropertiesAxiom axiom) {
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLHasKeyAxiom owlHasKeyAxiom) {
        return log(owlHasKeyAxiom);
    }

    public Set<? extends OWLAxiom> visit(OWLSubAnnotationPropertyOfAxiom owlSubAnnotationPropertyOfAxiom) {
        return log(owlSubAnnotationPropertyOfAxiom);
    }


    public Set<? extends OWLAxiom> visit(OWLAnnotationPropertyDomainAxiom owlAnnotationPropertyDomainAxiom) {
        return log(owlAnnotationPropertyDomainAxiom);
    }


    public Set<? extends OWLAxiom> visit(OWLAnnotationPropertyRangeAxiom owlAnnotationPropertyRangeAxiom) {
        return log(owlAnnotationPropertyRangeAxiom);
    }


    public Set<? extends OWLAxiom> visit(OWLObjectPropertyRangeAxiom axiom) {
        // Range(R, C) is syntactic sugar for Thing -> R only C
        Set<OWLAxiom> result = new HashSet<OWLAxiom>();
        for (OWLClassExpression rng : axiom.getRange().accept(tauGenerator)) {
            if (!isThing(rng)) {
                result.add(dataFactory.getOWLObjectPropertyRangeAxiom(axiom.getProperty(), rng));
            }
        }
        return log(axiom, result);
    }


    public Set<? extends OWLAxiom> visit(OWLObjectPropertyAssertionAxiom axiom) {
        // Here, we can weaken the checker by "striking out the object" - to do this, we
        // must use nominals and change the syntax to a subclass checker OR a class assertion!
        Set<OWLAxiom> result = new HashSet<OWLAxiom>(2);
        result.add(axiom);
        OWLClassExpression type = dataFactory.getOWLObjectSomeValuesFrom(axiom.getProperty(), dataFactory.getOWLThing());
        result.add(dataFactory.getOWLClassAssertionAxiom(type, axiom.getSubject()));
        return log(axiom, result);
    }


    public Set<? extends OWLAxiom> visit(OWLFunctionalObjectPropertyAxiom axiom) {
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLSubObjectPropertyOfAxiom axiom) {
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLDisjointUnionAxiom axiom) {
        // Hmmm
        System.err.println("WARNING: Not weakening disjoint union");
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLDeclarationAxiom axiom) {
        return Collections.singleton(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLAnnotationAssertionAxiom axiom) {
        return Collections.singleton(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLSymmetricObjectPropertyAxiom axiom) {
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLDataPropertyRangeAxiom axiom) {
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLFunctionalDataPropertyAxiom axiom) {
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLEquivalentDataPropertiesAxiom axiom) {
        // TODO: Data subprops
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLClassAssertionAxiom axiom) {
        // a : C is syntactic sugar for
        // {a} -> C, so we just use tau weakening on C
        Set<OWLAxiom> result = new HashSet<OWLAxiom>();
        for (OWLClassExpression cls : axiom.getClassExpression().accept(tauGenerator)) {
            if (!isThing(cls)) {
                result.add(dataFactory.getOWLClassAssertionAxiom(cls, axiom.getIndividual()));
            }
        }
        return log(axiom, result);
    }


    public Set<? extends OWLAxiom> visit(OWLEquivalentClassesAxiom axiom) {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        for (OWLClassExpression descA : axiom.getClassExpressions()) {
            for (OWLClassExpression descB : axiom.getClassExpressions()) {
                if (!descA.equals(descB)) {
                    axioms.addAll(dataFactory.getOWLSubClassOfAxiom(descA, descB).accept(this));
                }
            }
        }
        return log(axiom, axioms);
    }


    public Set<? extends OWLAxiom> visit(OWLDataPropertyAssertionAxiom axiom) {
        // Here, we can weaken the checker by "striking out the object" - to do this, we
        // must use nominals and change the syntax to a subclass checker
        Set<OWLAxiom> result = new HashSet<OWLAxiom>(2);
        result.add(axiom);
//        OWLClassExpression nominal = dataFactory.getOWLObjectOneOf(axiom.getSubject());
//        OWLClassExpression type = dataFactory.getOWLDataSomeValuesFrom(axiom.getProperty(), dataFactory.getTopDatatype());
//        result.add(dataFactory.getOWLSubClassOfAxiom(nominal, type));
        return log(axiom, result);
    }


    public Set<? extends OWLAxiom> visit(OWLTransitiveObjectPropertyAxiom axiom) {
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLSubDataPropertyOfAxiom axiom) {
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLSameIndividualAxiom axiom) {
        // Pairwise or setree?
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLSubPropertyChainOfAxiom axiom) {
        return log(axiom);
    }


    public Set<? extends OWLAxiom> visit(OWLInverseObjectPropertiesAxiom axiom) {
        // Implication
        Set<OWLAxiom> result = new HashSet<OWLAxiom>();
        result.add(dataFactory.getOWLSubObjectPropertyOfAxiom(axiom.getFirstProperty(), dataFactory.getOWLObjectInverseOf(axiom.getSecondProperty())));
        result.add(dataFactory.getOWLSubObjectPropertyOfAxiom(axiom.getSecondProperty(), dataFactory.getOWLObjectInverseOf(axiom.getFirstProperty())));
        return log(axiom, result);
    }

    public Set<? extends OWLAxiom> visit(OWLDatatypeDefinitionAxiom axiom) {
        return null;
    }

    public Set<? extends OWLAxiom> visit(SWRLRule rule) {
        return log(rule);
    }


}

