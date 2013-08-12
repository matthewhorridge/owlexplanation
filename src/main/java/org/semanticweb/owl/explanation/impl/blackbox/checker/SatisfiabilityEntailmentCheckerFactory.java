package org.semanticweb.owl.explanation.impl.blackbox.checker;

import org.semanticweb.owl.explanation.impl.blackbox.EntailmentChecker;
import org.semanticweb.owl.explanation.impl.blackbox.EntailmentCheckerFactory;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
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
 */
public class SatisfiabilityEntailmentCheckerFactory implements EntailmentCheckerFactory<OWLAxiom> {

    private OWLReasonerFactory reasonerFactory;

    private boolean useModularisation;

    private long entailmentCheckTimeOutMS = Long.MAX_VALUE;

    public SatisfiabilityEntailmentCheckerFactory(OWLReasonerFactory reasonerFactory) {
        this(reasonerFactory, true);
    }

    public SatisfiabilityEntailmentCheckerFactory(OWLReasonerFactory reasonerFactory, long entailmentCheckTimeOutMS) {
        this.reasonerFactory = reasonerFactory;
        this.entailmentCheckTimeOutMS = entailmentCheckTimeOutMS;
        this.useModularisation = true;
    }

    public SatisfiabilityEntailmentCheckerFactory(OWLReasonerFactory reasonerFactory, boolean useModularisation) {
        this.reasonerFactory = reasonerFactory;
        this.useModularisation = useModularisation;
    }

    public SatisfiabilityEntailmentCheckerFactory(OWLReasonerFactory reasonerFactory, boolean useModularisation, long entailmentCheckTimeOutMS) {
        this.reasonerFactory = reasonerFactory;
        this.useModularisation = useModularisation;
        this.entailmentCheckTimeOutMS = entailmentCheckTimeOutMS;
    }

    public EntailmentChecker<OWLAxiom> createEntailementChecker(OWLAxiom entailment) {
        return new SatisfiabilityEntailmentChecker(reasonerFactory, entailment, useModularisation, entailmentCheckTimeOutMS);
    }
}
