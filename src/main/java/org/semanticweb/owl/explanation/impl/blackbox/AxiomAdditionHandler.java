package org.semanticweb.owl.explanation.impl.blackbox;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Set;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 06/05/2011
 */
public interface AxiomAdditionHandler {

    void addExtraAxioms(Set<OWLAxiom> axioms);
}
