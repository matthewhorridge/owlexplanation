package org.semanticweb.owl.explanation.impl.blackbox.checker;

import org.semanticweb.owl.explanation.impl.blackbox.EntailmentChecker;
import org.semanticweb.owl.explanation.impl.blackbox.EntailmentCheckerFactory;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 14/08/2012
 */
public class PatternBasedConsistencyEntailmentCheckerFactory implements EntailmentCheckerFactory<OWLAxiom> {

    private OWLReasonerFactory rf;

    private long timeout;

    public PatternBasedConsistencyEntailmentCheckerFactory(OWLReasonerFactory rf, long timeout) {
        this.rf = rf;
        this.timeout = timeout;
    }

    public EntailmentChecker<OWLAxiom> createEntailementChecker(OWLAxiom entailment) {
        return new PatternBasedConsistencyEntailmentChecker(rf, timeout);
    }
}
