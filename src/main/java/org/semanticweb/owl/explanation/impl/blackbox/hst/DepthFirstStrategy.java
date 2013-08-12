package org.semanticweb.owl.explanation.impl.blackbox.hst;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.HashSet;
import java.util.Set;
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
 * Date: 18-Feb-2010
 */
public class DepthFirstStrategy<E> implements HittingSetTreeConstructionStrategy<E> {


    public void start(HittingSetTree<E> hittingSetTree) {
    }

    public void constructTree(HittingSetTree<E> hittingSetTree, int limit, ExplanationGeneratorMediator<E> handler) {
        buildHittingSetTree(hittingSetTree, limit, handler, hittingSetTree.getRoot());
    }

    public void buildHittingSetTree(HittingSetTree<E> hittingSetTree, int limit, ExplanationGeneratorMediator<E> handler, HittingSetTreeNode<E> currentNode) {
        for (OWLAxiom ax : currentNode.getExplanation().getAxioms()) {
            handler.removeAxiom(ax);
            Set<OWLAxiom> pathContents = new HashSet<OWLAxiom>(currentNode.getPathToRoot());
            pathContents.add(ax);
            if (hittingSetTree.addExploredPath(pathContents)) {
                // Look to reuse a justification
                Explanation<E> expl = getNonIntersectingExplanation(hittingSetTree, pathContents);
                boolean reuse = true;
                if (expl == null) {
                    reuse = false;
                    expl = handler.generateExplanation(currentNode.getExplanation().getEntailment());
                    hittingSetTree.addExplanation(expl);
                    if(hittingSetTree.getExplanations().size() == limit) {
                        return;
                    }
                }
                if (!expl.isEmpty()) {
                    HittingSetTreeNode<E> hittingSetTreeNode = new HittingSetTreeNode<E>(hittingSetTree, ax, currentNode, expl, reuse);
                    currentNode.addChild(ax, hittingSetTreeNode);
                    buildHittingSetTree(hittingSetTree, limit, handler, hittingSetTreeNode);
                }
                else {
                  hittingSetTree.addClosedPath(new HashSet<OWLAxiom>(pathContents));
                }
            }handler.addAxiom(ax);
        }
    }

    private Explanation<E> getNonIntersectingExplanation(HittingSetTree<E> hittingSetTree, Set<OWLAxiom> pathContents) {
        for (Explanation<E> existingExpl : hittingSetTree.getExplanations()) {
            boolean overlaps = false;
            // Does the explanation intersect with out current path? If not, we can reuse it
            for (OWLAxiom pathAx : pathContents) {
                if (existingExpl.contains(pathAx)) {
                    // Overlap
                    overlaps = true;
                    break;
                }
            }
            if (!overlaps) {
                return existingExpl;
            }
        }
        return null;
    }

    public void finish(HittingSetTree<E> hittingSetTree) {
    }
}
