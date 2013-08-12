package org.semanticweb.owl.explanation.impl.blackbox.checker;

import org.semanticweb.owl.explanation.api.ExplanationGenerator;
import org.semanticweb.owl.explanation.api.ExplanationGeneratorFactory;
import org.semanticweb.owl.explanation.api.ExplanationProgressMonitor;
import org.semanticweb.owl.explanation.impl.blackbox.BlackBoxExplanationGenerator2;
import org.semanticweb.owl.explanation.impl.blackbox.Configuration;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.HashSet;
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
 * A factory that creates a black box explanation generator.
 */
public class BlackBoxExplanationGeneratorFactory<E> implements ExplanationGeneratorFactory<E> {


    private Configuration<E> configuration;


    public BlackBoxExplanationGeneratorFactory(Configuration<E> configuration) {
        this.configuration = configuration;
    }

    public ExplanationGenerator<E> createExplanationGenerator(OWLOntology ontology) {
        return createExplanationGenerator(ontology, null);
    }

    public ExplanationGenerator<E> createExplanationGenerator(OWLOntology ontology, ExplanationProgressMonitor<E> progressMonitor) {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>(ontology.getLogicalAxiomCount());
        for(OWLOntology ont : ontology.getImportsClosure()) {
            axioms.addAll(ont.getLogicalAxioms());
        }
        return new BlackBoxExplanationGenerator2<E>(
                axioms,
                configuration.getCheckerFactory(),
                configuration.getExpansionStrategy(),
                configuration.getContractionStrategy(),
                progressMonitor);
    }

    public ExplanationGenerator<E> createExplanationGenerator(Set<? extends OWLAxiom> axioms) {
        return createExplanationGenerator(axioms, null);
    }

    public ExplanationGenerator<E> createExplanationGenerator(Set<? extends OWLAxiom> axioms, ExplanationProgressMonitor<E> progressMonitor) {
        return new BlackBoxExplanationGenerator2<E>(
                axioms,
                configuration.getCheckerFactory(),
                configuration.getExpansionStrategy(),
                configuration.getContractionStrategy(),
                progressMonitor);
    }
}
