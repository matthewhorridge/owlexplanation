package org.semanticweb.owl.explanation.api;

import java.util.Set;/*
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
 * Author: Matthew Horridge<br> The University of Manchester<br> Information Management Group<br>
 * Date: 28-Nov-2008
 */
public interface ExplanationProgressMonitor<E> {

    /**
     * Called by explanation generators that support progress monitors.  This is
     * called when a new explanation is found for an entailment when searching for
     * multiple explanations.
     *
     * @param generator            The explanation generator that found the explanation
     * @param explanation          The explanation that was found
     *                             for the entailment or <code>false</code> if the explanation generator should stop finding explanations
     *                             at the next opportunity.
     * @param allFoundExplanations All of the explanations found so far for the specified entailment
     */
    void foundExplanation(ExplanationGenerator<E> generator, Explanation<E> explanation, Set<Explanation<E>> allFoundExplanations);

    /**
     * The explanation generator will periodically check to see if it should continue finding explanations by calling
     * this method.
     *
     * @return <code>true</code> if the explanation generator should cancel the explanation finding process or <code>false</code>
     *         if the explanation generator should continue.
     */
    boolean isCancelled();
}
