package org.semanticweb.owl.explanation.api;

import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Set;
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
 * 03-Sep-2008<br><br>
 *
 * An interface that describes a factory that can create an explanation generator.  It is assumed that an explanation
 * generator generates explanations from a set of source axioms that are obtained from a set of ontolgies.
 */
public interface ExplanationGeneratorFactory<E> {

    /**
     * Creates an explanation generator that draws source axioms for the explanation from an ontology and its imports
     * closure.
     *
     * @param ontology The ontology from which the source axioms are obtained.
     * @return An explanation generator that generates explanations based on the axioms in the imports closure
     * of the specified ontology
     */
    ExplanationGenerator<E> createExplanationGenerator(OWLOntology ontology);


    /**
     * Creates an explanation generator that draws source axioms for the explanation from an ontology and its imports
     * closure.
     *
     * @param ontology The ontology from which the source axioms are obtained.
     * @param progressMonitor A progress monitor that gets informed of when explanations are found (should not be
     * <code>null</code>)
     * @return An explanation generator that generates explanations based on the axioms in the imports closure
     * of the specified ontology
     */
    ExplanationGenerator<E> createExplanationGenerator(OWLOntology ontology, ExplanationProgressMonitor<E> progressMonitor);

    /**
     * Creates an explanation generator that generates explanations for entailments that hold over the specified set
     * of axioms
     * @param axioms The axioms that give rise to the entailments
     * @return An explanation generator that generates explanations based on the specified set of axioms
     */
    ExplanationGenerator<E> createExplanationGenerator(Set<? extends OWLAxiom> axioms);


    /**
     * Creates an explanation generator that generates explanations for entailments that hold over the specified set
     * of axioms
     * @param axioms The axioms that give rise to the entailments
     * @param progressMonitor A progress monitor that gets informed of when explanations are found (should not be
     * <code>null</code>)
     * @return An explanation generator that generates explanations based on the specified set of axioms
     */
    ExplanationGenerator<E> createExplanationGenerator(Set<? extends OWLAxiom> axioms, ExplanationProgressMonitor<E> progressMonitor);

}
