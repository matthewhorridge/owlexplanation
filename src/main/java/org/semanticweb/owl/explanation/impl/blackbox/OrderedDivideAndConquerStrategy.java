package org.semanticweb.owl.explanation.impl.blackbox;

import org.semanticweb.owl.explanation.api.ExplanationProgressMonitor;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.util.AxiomSubjectProvider;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 13/01/2011
 */
public class OrderedDivideAndConquerStrategy implements ContractionStrategy {

    private DivideAndConquerContractionStrategy delegate = new DivideAndConquerContractionStrategy();

    private int count = 0;

    public Set<OWLAxiom> doPruning(Set<OWLAxiom> axioms, EntailmentChecker checker, ExplanationProgressMonitor<?> progressMonitor) {
        count = 0;
        LinkedHashSet<OWLAxiom> orderedAxioms = new LinkedHashSet<OWLAxiom>();
        AxiomSubjectProvider subjectProvider = new AxiomSubjectProvider();
        Map<OWLObject, Set<OWLAxiom>> axiomsBySubject = new HashMap<OWLObject, Set<OWLAxiom>>();
        for(OWLAxiom ax : axioms) {
            OWLObject object = subjectProvider.getSubject(ax);
            Set<OWLAxiom> axiomsSet = axiomsBySubject.get(object);
            axiomsBySubject.put(object, axiomsSet);
        }
        return delegate.doPruning(orderedAxioms, checker, progressMonitor);
    }

    public int getNumberOfSteps() {
        return delegate.getNumberOfSteps();
    }
}
