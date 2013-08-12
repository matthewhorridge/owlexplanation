package org.semanticweb.owl.explanation.impl.masking;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.ExplanationGenerator;
import org.semanticweb.owl.explanation.api.ExplanationGeneratorFactory;
import org.semanticweb.owl.explanation.impl.laconic.LaconicExplanationGeneratorFactory;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.StructuralTransformation;

import java.util.Set;
/*
 * Copyright (C) 2010, University of Manchester
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
 * Date: 28-Jan-2010
 */
public class MaskingDetectorDeltaImpl implements MaskingDetector {

    private OWLOntologyManager manager;

    private OWLReasonerFactory reasonerFactory;

    private LaconicExplanationGeneratorFactory<OWLAxiom> expGenFac;

    public MaskingDetectorDeltaImpl(OWLOntologyManager manager, LaconicExplanationGeneratorFactory<OWLAxiom> expGenFac, OWLReasonerFactory reasonerFactory) {
        this.manager = manager;
        this.reasonerFactory = reasonerFactory;
        this.expGenFac = expGenFac;
    }

    public boolean isMaskingPresent(Explanation<OWLAxiom> explanation) {
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        StructuralTransformation st = new StructuralTransformation(dataFactory);
        Set<OWLAxiom> axioms = st.getTransformedAxioms(explanation.getAxioms());
        ExplanationGenerator<OWLAxiom> expGen = expGenFac.createExplanationGenerator(axioms);
        Set<Explanation<OWLAxiom>> expls = expGen.getExplanations(explanation.getEntailment());
        return expls.size() > 1;
    }
}
