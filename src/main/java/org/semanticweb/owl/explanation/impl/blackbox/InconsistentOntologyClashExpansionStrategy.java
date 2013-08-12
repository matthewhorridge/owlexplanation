package org.semanticweb.owl.explanation.impl.blackbox;

import org.semanticweb.owl.explanation.api.ExplanationProgressMonitor;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owl.explanation.impl.blackbox.checker.ConsistencyEntailmentChecker;

import java.util.Set;
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
 * Date: 24-May-2009
 */
public class InconsistentOntologyClashExpansionStrategy implements ExpansionStrategy {

    private int count = 0;

    private StructuralTypePriorityExpansionStrategy strategy = new StructuralTypePriorityExpansionStrategy();

    private InconsistentOntologyExpansionStrategy defaultStrategy = new InconsistentOntologyExpansionStrategy();
    
    public Set<OWLAxiom> doExpansion(Set<OWLAxiom> axioms, EntailmentChecker checker, ExplanationProgressMonitor<?> progressMonitor) {
        return defaultStrategy.doExpansion(axioms, checker, progressMonitor);
    }

    public int getNumberOfSteps() {
        return 1;
    }
}
