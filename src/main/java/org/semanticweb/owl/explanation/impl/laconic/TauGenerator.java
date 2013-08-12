package org.semanticweb.owl.explanation.impl.laconic;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.CollectionFactory;

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
 * 15-Sep-2008<br><br>
 */
public class TauGenerator extends BaseDescriptionGenerator {


    public TauGenerator(OWLDataFactory factory) {
        super(factory);
    }


    public Set<OWLClassExpression> visit(OWLClass desc) {
        Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
        descs.add(desc);
        descs.add(getDataFactory().getOWLThing());
        return descs;
    }


    public Set<OWLClassExpression> visit(OWLObjectComplementOf desc) {
        Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
        for (OWLClassExpression d : computeBeta(desc.getOperand())) {
            descs.add(getDataFactory().getOWLObjectComplementOf(d));
        }
        return descs;
    }

    public Set<OWLClassExpression> visit(OWLObjectMaxCardinality desc) {
        // We need to increase the cardinality to some upper bound, but
        // how the hell do we figure out this?
        // Filler gets SMALLER
        Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
        for (OWLClassExpression filler : computeBeta(desc.getFiller())) {
            descs.add(getDataFactory().getOWLObjectMaxCardinality(desc.getCardinality(), desc.getProperty(), filler));
        }
        descs.add(getLimit());
        return descs;
    }


    public Set<OWLClassExpression> visit(OWLObjectMinCardinality desc) {
        // Weaken by decreasing numbers and weakening fillers for all
        // combinations!
        // Filler gets BIGGER
        Set<OWLClassExpression> weakenedFillers = computeTau(desc.getFiller());
        Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
        // Greater than 0 since min 0 R is TOP
        for (int n = desc.getCardinality(); n > 0; n--) {
            for (OWLClassExpression filler : weakenedFillers) {
                result.add(getDataFactory().getOWLObjectMinCardinality(n, desc.getProperty(), filler));
            }
        }
        result.add(getLimit());
        return result;
    }

    @Override
    public Set<OWLClassExpression> visit(OWLObjectUnionOf desc) {
        boolean anon = false;
        for (OWLClassExpression ce : desc.asDisjunctSet()) {
            if (ce.isAnonymous()) {
                anon = true;
                break;
            }
        }
        if (anon) {
            return super.visit(desc);
        }
        else {
            return CollectionFactory.createSet(desc, getLimit());
        }
    }

    protected OWLClass getLimit() {
        return getDataFactory().getOWLThing();
    }


    protected OWLDataRange getDataLimit() {
        return getDataFactory().getTopDatatype();
    }
}
