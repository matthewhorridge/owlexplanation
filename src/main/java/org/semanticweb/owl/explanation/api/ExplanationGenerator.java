package org.semanticweb.owl.explanation.api;

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
 * Represents a general purpose interface for obtaining explanations. The interface does not commit to the type of
 * entailment for which an explanation is to be generated.  It is also assumed that there could be more than one
 * explanation for a given entailment.
 */
public interface ExplanationGenerator<E> {

    /**
     * Gets explanations for an entailment.  All explanations for the entailment will be returned.
     *
     * @param entailment The entailment for which explanations will be generated.
     * @return A set containing all of the explanations.  The set will be empty if the entailment does not hold.
     * @throws ExplanationException if there was a problem generating the explanation.
     */
    Set<Explanation<E>> getExplanations(E entailment) throws ExplanationException;


    /**
     * Gets explanations for an entailment, with limit on the number of explanations returned.
     *
     * @param entailment The entailment for which explanations will be generated.
     * @param limit      The maximum number of explanations to generate. This should be a positive integer.
     * @return A set containing explanations.  The maximum cardinality of the set is specified by the limit parameter.
     *         The set may be empty if the entailment does not hold, or if a limit of zero or less is supplied.
     * @throws ExplanationException if there was a problem generating the explanation.
     */
    Set<Explanation<E>> getExplanations(E entailment, int limit) throws ExplanationException;
    

}
