package org.semanticweb.owl.explanation.impl.util;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Set;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 23/04/2011
 */
public interface AxiomTransformation {

    Set<OWLAxiom> transform(Set<OWLAxiom> axioms);
}
