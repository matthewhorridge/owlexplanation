package org.semanticweb.owl.explanation.impl.blackbox.hst;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.ExplanationGeneratorInterruptedException;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.*;
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
public class BreadthFirstStrategy<E> implements HittingSetTreeConstructionStrategy<E> {


    public void start(HittingSetTree<E> hittingSetTree) {
    }

    public void constructTree(HittingSetTree<E> hittingSetTree, int limit, ExplanationGeneratorMediator<E> handler) {
        if (hittingSetTree.getProgressMonitor().isCancelled()) {
            throw new ExplanationGeneratorInterruptedException();
        }

        List<HittingSetTreeNode<E>> queue = new ArrayList<HittingSetTreeNode<E>>();
        queue.add(hittingSetTree.getRoot());
        boolean b = true;
        while (b) {
            b = buildHittingSetTree(hittingSetTree, limit, handler, queue);
            if (hittingSetTree.getProgressMonitor().isCancelled()) {
                throw new ExplanationGeneratorInterruptedException();
            }
        }
    }

    private boolean rebuilt = false;

    public boolean buildHittingSetTree(HittingSetTree<E> hittingSetTree, int limit, ExplanationGeneratorMediator<E> handler, List<HittingSetTreeNode<E>> queue) {
        while (!queue.isEmpty()) {
            if (hittingSetTree.getProgressMonitor().isCancelled()) {
                throw new ExplanationGeneratorInterruptedException();
            }
            HittingSetTreeNode<E> currentNode = queue.remove(0);

            Set<OWLAxiom> nodeAxioms = currentNode.getExplanation().getAxioms();
            for (OWLAxiom ax : nodeAxioms) {
                if (hittingSetTree.getProgressMonitor().isCancelled()) {
                    throw new ExplanationGeneratorInterruptedException();
                }

                Set<OWLAxiom> pathContents = new HashSet<OWLAxiom>(currentNode.getPathToRoot());

                // Extend the path contents
                pathContents.add(ax);

                if (!hittingSetTree.containsClosedPath(pathContents)) {
                    // Add the path - this checks to see if we are already exploring this path.  If we are,
                    // we don't need to explore it again.
                    if (hittingSetTree.addExploredPath(pathContents)) {
                        // The path hadn't already been explored
                        // Remove all axioms in the path.
                        for (OWLAxiom pathAx : pathContents) {
                            handler.removeAxiom(pathAx);
                        }

                        // See if we can reuse a justification.
                        Explanation<E> expl = getNonIntersectingExplanation(hittingSetTree, pathContents, queue);
                        boolean reuse = true;
                        if (expl == null) {
                            reuse = false;
                            hittingSetTree.incrementNumberOfNodesWithCallsToFindOne();
                            expl = handler.generateExplanation(currentNode.getExplanation().getEntailment());
                            hittingSetTree.addExplanation(expl);
                            if (hittingSetTree.getExplanations().size() == limit) {
                                return false;
                            }
                        }
                        else {
                            // Justification reuse!
                            hittingSetTree.incrementNumberOfNodesWithReusedJustifications();
                        }

                        if (!expl.isEmpty()) {
                            HittingSetTreeNode<E> hittingSetTreeNode = new HittingSetTreeNode<E>(hittingSetTree, ax, currentNode, expl, reuse);
                            currentNode.addChild(ax, hittingSetTreeNode);
                            queue.add(hittingSetTreeNode);
                        }
                        else {
                            // Save some space - don't add empty leaf nodes
                            hittingSetTree.addClosedPath(new HashSet<OWLAxiom>(pathContents));
                        }

                        // Add path contents back in
                        for (OWLAxiom pathAx : pathContents) {
                            handler.addAxiom(pathAx);
                        }
                    }
                }
                else {

                }
            }
        }
        return false;
    }

    private Explanation<E> getNonIntersectingExplanation(HittingSetTree<E> hittingSetTree, Set<OWLAxiom> pathContents, List<HittingSetTreeNode<E>> queue) {


        List<Explanation<E>> explanations = hittingSetTree.getSortedExplanations();

        // Choose an explanation that contains axioms that are contained in other openFromFolder paths

        int pathsToExploreMinimum = Integer.MAX_VALUE;

        Explanation<E> currentCandidate = null;


        for (Explanation<E> existingExpl : explanations) {
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

//                // It is a candidate
//                int pathsToExploreForCurrentNode = getNumberOfPathsToExploreForCandidateExplanation(hittingSetTree, pathContents, existingExpl);
//                if(pathsToExploreForCurrentNode < pathsToExploreMinimum) {
//                    pathsToExploreMinimum = pathsToExploreForCurrentNode;
//                    currentCandidate = existingExpl;
//                }
            }
        }
        return currentCandidate;

    }

//    private int getNumberOfPathsToExploreForCandidateExplanation(HittingSetTree<E> hittingSetTree, Set<OWLAxiom> pathContents, Explanation<E> existingExpl) {
//        int pathsToExploreForCurrentNode = existingExpl.getAxioms().size();
//        Set<OWLAxiom> currentNodePath = new HashSet<OWLAxiom>(pathContents);
//        for (OWLAxiom currentNodeAx : existingExpl.getAxioms()) {
//            currentNodePath.add(currentNodeAx);
//            if (hittingSetTree.isExplored(currentNodePath)) {
//                pathsToExploreForCurrentNode--;
//            }
//            currentNodePath.remove(currentNodeAx);
//        }
//        return pathsToExploreForCurrentNode;
//    }

    public void finish(HittingSetTree<E> hittingSetTree) {
    }


}
