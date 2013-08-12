package org.semanticweb.owl.explanation.impl.blackbox.checker;

import org.semanticweb.owl.explanation.impl.blackbox.EntailmentChecker;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.*;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 14/08/2012
 */
public class PatternBasedConsistencyEntailmentChecker implements EntailmentChecker<OWLAxiom> {

    private ConsistencyEntailmentChecker delegate;
    
    private OWLDataFactory df;
    
    public PatternBasedConsistencyEntailmentChecker(OWLReasonerFactory rf, long timeout) {
        delegate = new ConsistencyEntailmentChecker(rf, timeout);
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        df = man.getOWLDataFactory();
    }

    private int counter = 0;
    
    public int getCounter() {
        return counter;
    }

    public void resetCounter() {
        counter = 0;
    }

    public OWLAxiom getEntailment() {
        return delegate.getEntailment();
    }

    public Set<OWLEntity> getEntailmentSignature() {
        return delegate.getEntailmentSignature();
    }

    public Set<OWLEntity> getSeedSignature() {
        return Collections.emptySet();
    }

    public boolean isEntailed(Set<OWLAxiom> axioms) {
       
        Map<OWLIndividual, Map<OWLDataPropertyExpression, Set<OWLLiteral>>> subjPredObjectMap = new HashMap<OWLIndividual, Map<OWLDataPropertyExpression, Set<OWLLiteral>>>();
        for(OWLAxiom ax : axioms) {
            if(ax instanceof OWLDataPropertyAssertionAxiom) {
                OWLDataPropertyExpression prop = ((OWLDataPropertyAssertionAxiom) ax).getProperty();
                OWLFunctionalDataPropertyAxiom funcAx = df.getOWLFunctionalDataPropertyAxiom(prop);
                if(axioms.contains(funcAx)) {
                    OWLIndividual subject = ((OWLDataPropertyAssertionAxiom) ax).getSubject();
                    Map<OWLDataPropertyExpression, Set<OWLLiteral>> propObjectMap = subjPredObjectMap.get(subject);
                    if(propObjectMap == null) {
                        propObjectMap = new HashMap<OWLDataPropertyExpression, Set<OWLLiteral>>();
                        subjPredObjectMap.put(subject, propObjectMap);
                    }
                    Set<OWLLiteral> objects = propObjectMap.get(prop);
                    if(objects == null) {
                        objects = new HashSet<OWLLiteral>();
                        propObjectMap.put(prop, objects);
                    }
                    OWLLiteral object = ((OWLDataPropertyAssertionAxiom) ax).getObject();
                    objects.add(object);
                    if(objects.size() > 1) {
                        Set<OWLAxiom> candidateJustification = new HashSet<OWLAxiom>(3);
                        candidateJustification.add(funcAx);
                        for(OWLLiteral lit : objects) {
                            candidateJustification.add(df.getOWLDataPropertyAssertionAxiom(prop, subject, lit));
                        }
                        if(delegate.isEntailed(candidateJustification)) {
                            return true;
                        }

                    }
                }
            }
        }
        return delegate.isEntailed(axioms);
    }

    public Set<OWLAxiom> getModule(Set<OWLAxiom> axioms) {
        return delegate.getModule(axioms);
    }


    public String getModularisationTypeDescription() {
        return delegate.getModularisationTypeDescription();
    }

    public boolean isUseModularisation() {
        return delegate.isUseModularisation();
    }

    public Set<OWLAxiom> getEntailingAxioms(Set<OWLAxiom> axioms) {
        return delegate.getEntailingAxioms(axioms);
    }
}
