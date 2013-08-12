package org.semanticweb.owl.explanation.api;
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
 * An explanation exception that describes the situation where an entailment
 * does not hold in a set of source axioms.
 */
public class NotEntailedException extends ExplanationException {

    private Object entailment;


    public NotEntailedException(Object entailment) {
        super("Not entailed: " + entailment);
        this.entailment = entailment;
    }


    /**
     * Gets the object that represents the desired entailment which is in fact not entailed.
     * @return The desired entailment.
     */
    public Object getEntailment() {
        return entailment;
    }
}
