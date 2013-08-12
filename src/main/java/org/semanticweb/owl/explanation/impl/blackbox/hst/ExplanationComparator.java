package org.semanticweb.owl.explanation.impl.blackbox.hst;

import org.semanticweb.owl.explanation.api.Explanation;

import java.util.Comparator;
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
 * Date: 07-Mar-2010
 */
public class ExplanationComparator<E> implements Comparator<Explanation<E>> {


    public ExplanationComparator(HittingSetTree<E> hittingSetTree) {
    }

    public int compare(Explanation<E> o1, Explanation<E> o2) {
        if(o1.equals(o2)) {
            return 0;
        }
        int size1 = o1.getAxioms().size();
        int size2 = o1.getAxioms().size();
        // We want the smallest one to come first
        int sizeDifference = size2 - size1;
        if(sizeDifference != 0) {
            return sizeDifference;
        }
        // Same size

        // Now we would prefer ones that contained
        

        return -1;
    }
}
