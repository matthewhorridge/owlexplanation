package org.semanticweb.owl.explanation.impl.blackbox;

import org.semanticweb.owl.explanation.api.ExplanationProgressMonitor;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Set;
import java.util.Collections;
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
 * 18-Sep-2008<br><br>
 *
 * An expansion strategy used for testing.  All of the axioms are added in one go.
 */
public class AllInOneExpansionStrategy implements ExpansionStrategy {


    public Set<OWLAxiom> doExpansion(Set<OWLAxiom> axioms, EntailmentChecker checker, ExplanationProgressMonitor<?> progressMonitor) {
        if(checker.isEntailed(axioms)) {
            return axioms;
        }
        else {
            return Collections.emptySet();
        }
    }

    public int getNumberOfSteps() {
        return 1;
    }
}
