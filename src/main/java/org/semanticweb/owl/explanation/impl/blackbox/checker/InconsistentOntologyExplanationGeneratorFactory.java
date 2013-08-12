package org.semanticweb.owl.explanation.impl.blackbox.checker;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owl.explanation.api.ExplanationGeneratorFactory;
import org.semanticweb.owl.explanation.api.ExplanationGenerator;
import org.semanticweb.owl.explanation.api.ExplanationProgressMonitor;
import org.semanticweb.owl.explanation.api.NullExplanationProgressMonitor;
import org.semanticweb.owl.explanation.impl.blackbox.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.util.Set;
import java.util.HashSet;
/*
 * Copyright (C) 2009, University of Manchester
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
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Information Management Group<br>
 * Date: 14-May-2009
 */
public class InconsistentOntologyExplanationGeneratorFactory implements ExplanationGeneratorFactory<OWLAxiom> {


    private InconsistentOntologyContractionStrategy contractionStrategy;

    private ExpansionStrategy expansionStrategy;

    private ConsistencyEntailmentCheckerFactory consistencyEntailmentCheckerFactory;

    public InconsistentOntologyExplanationGeneratorFactory(OWLReasonerFactory reasonerFactory, long entailmentCheckingTimeout) {
        expansionStrategy = new InconsistentOntologyExpansionStrategy();
//        expansionStrategy = new InconsistentOntologyClashExpansionStrategy();
        contractionStrategy = new InconsistentOntologyContractionStrategy();
        consistencyEntailmentCheckerFactory = new ConsistencyEntailmentCheckerFactory(reasonerFactory, entailmentCheckingTimeout);
    }


    public ExplanationGenerator<OWLAxiom> createExplanationGenerator(OWLOntology ontology) {
        return createExplanationGenerator(ontology, new NullExplanationProgressMonitor<OWLAxiom>());
    }

    public ExplanationGenerator<OWLAxiom> createExplanationGenerator(OWLOntology ontology, ExplanationProgressMonitor<OWLAxiom> progressMonitor) {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>(ontology.getLogicalAxiomCount());
        for(OWLOntology ont : ontology.getImportsClosure()) {
            axioms.addAll(ont.getLogicalAxioms());
        }
        return new BlackBoxExplanationGenerator2<OWLAxiom>(
                axioms,
                consistencyEntailmentCheckerFactory,
                expansionStrategy,
                contractionStrategy,
                progressMonitor);
    }

    public ExplanationGenerator<OWLAxiom> createExplanationGenerator(Set<? extends OWLAxiom> axioms) {
        return createExplanationGenerator(axioms, new NullExplanationProgressMonitor<OWLAxiom>());
    }

    public ExplanationGenerator<OWLAxiom> createExplanationGenerator(Set<? extends OWLAxiom> axioms, ExplanationProgressMonitor<OWLAxiom> progressMonitor) {
        return new BlackBoxExplanationGenerator2<OWLAxiom>(
                axioms,
                consistencyEntailmentCheckerFactory,
                expansionStrategy,
                contractionStrategy,
                progressMonitor);
    }
}
