package org.semanticweb.owl.explanation.impl.laconic;

import org.semanticweb.owlapi.model.*;
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
public class TriviallyBottomChecker implements OWLClassExpressionVisitorEx<Boolean> {


    public Boolean visit(OWLClass desc) {
        return desc.isOWLNothing();
    }


    public Boolean visit(OWLObjectIntersectionOf desc) {
        for(OWLClassExpression conjunct : desc.getOperands()) {
            if(conjunct.accept(this)) {
                return true;
            }
        }
        return false;
    }


    public Boolean visit(OWLObjectUnionOf desc) {
        for(OWLClassExpression conjunct : desc.getOperands()) {
            if(!conjunct.accept(this)) {
                return false;
            }
        }
        return true;
    }


    public Boolean visit(OWLObjectComplementOf desc) {
        return desc.isOWLThing();
    }


    public Boolean visit(OWLObjectSomeValuesFrom desc) {
        return desc.getFiller().accept(this);
    }


    public Boolean visit(OWLObjectAllValuesFrom desc) {
        return false;
    }


    public Boolean visit(OWLObjectHasValue desc) {
        return false;
    }


    public Boolean visit(OWLObjectMinCardinality desc) {
        return desc.getFiller().accept(this);
    }


    public Boolean visit(OWLObjectExactCardinality desc) {
        return desc.getFiller().accept(this);
    }


    public Boolean visit(OWLObjectMaxCardinality desc) {
        return false;
    }


    public Boolean visit(OWLObjectHasSelf desc) {
        return false;
    }


    public Boolean visit(OWLObjectOneOf desc) {
        return false;
    }


    public Boolean visit(OWLDataSomeValuesFrom desc) {
        return false;
    }


    public Boolean visit(OWLDataAllValuesFrom desc) {
        return false;
    }


    public Boolean visit(OWLDataHasValue desc) {
        return false;
    }


    public Boolean visit(OWLDataMinCardinality desc) {
        return false;
    }


    public Boolean visit(OWLDataExactCardinality desc) {
        return false;
    }


    public Boolean visit(OWLDataMaxCardinality desc) {
        return false;
    }
}
