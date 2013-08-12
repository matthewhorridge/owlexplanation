package org.semanticweb.owl.explanation.impl.blackbox;

import org.semanticweb.owl.explanation.api.ExplanationProgressMonitor;
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
 * A configuration that describes how a black box explanation generator should be configured.
 * There are three main options: 1) The type of entailment checker that should be used, 2) The
 * expansion strategy that should be used, 3) The contraction strategy that should be used
 */
public class Configuration<E> {

    private EntailmentCheckerFactory<E> checkerFactory;

    private ExpansionStrategy expansionStrategy;

    private ContractionStrategy contractionStrategy;

    public Configuration(EntailmentCheckerFactory<E> checkerFactory, ExpansionStrategy expansionStrategy, ContractionStrategy contractionStrategy) {
        this(checkerFactory, expansionStrategy, contractionStrategy, null);
    }


    public Configuration(EntailmentCheckerFactory<E> checkerFactory, ExpansionStrategy expansionStrategy, ContractionStrategy contractionStrategy, ExplanationProgressMonitor<E> progressMonitor) {
        this.checkerFactory = checkerFactory;
        this.contractionStrategy = contractionStrategy;
        this.expansionStrategy = expansionStrategy;
    }


    public Configuration(EntailmentCheckerFactory<E> checkerFactory) {
        this(checkerFactory, new StructuralTypePriorityExpansionStrategy(), new DivideAndConquerContractionStrategy());
    }


    public Configuration(EntailmentCheckerFactory<E> checkerFactory, ExplanationProgressMonitor<E> progressMonitor) {
        this(checkerFactory, new StructuralTypePriorityExpansionStrategy(), new DivideAndConquerContractionStrategy(), progressMonitor);
    }


    public EntailmentCheckerFactory<E> getCheckerFactory() {
        return checkerFactory;
    }


    public ContractionStrategy getContractionStrategy() {
        return contractionStrategy;
    }


    public ExpansionStrategy getExpansionStrategy() {
        return expansionStrategy;
    }

}
