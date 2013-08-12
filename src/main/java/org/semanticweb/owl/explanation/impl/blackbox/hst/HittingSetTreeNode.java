package org.semanticweb.owl.explanation.impl.blackbox.hst;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.ArrayList;
import java.util.List;
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
public class HittingSetTreeNode<E> {

    private OWLAxiom parentEdgeLabel;

    private HittingSetTreeNode<E> parentNode;

    private Explanation<E> explanation;

    private List<HittingSetTreeNode<E>> children = new ArrayList<HittingSetTreeNode<E>>();

    private List<OWLAxiom> edgeLabels = new ArrayList<OWLAxiom>();

//    private HittingSetTree<E> tree;

    private boolean reuse;

    public HittingSetTreeNode(HittingSetTree<E> tree, OWLAxiom parentEdgeLabel, HittingSetTreeNode<E> parentNode, Explanation<E> explanation, boolean reuse) {
//        this.tree = tree;
        this.parentEdgeLabel = parentEdgeLabel;
        this.parentNode = parentNode;
        this.explanation = explanation;
        this.reuse = reuse;
    }

    public HittingSetTreeNode(HittingSetTree<E> tree, Explanation<E> explanation) {
//        this.tree = tree;
        this.explanation = explanation;
    }

    public HittingSetTreeNode(HittingSetTree<E> tree, OWLAxiom parentEdgeLabel, HittingSetTreeNode<E> parentNode, boolean reuse) {
//        this.tree = tree;
        this.parentEdgeLabel = parentEdgeLabel;
        this.parentNode = parentNode;
        this.reuse = reuse;
    }

    public boolean isReuse() {
        return reuse;
    }

    public Explanation<E> getExplanation() {
        return explanation;
    }

    public List<HittingSetTreeNode<E>> getChildren() {
        return children;
//        throw new RuntimeException();
    }

    public void addChild(OWLAxiom edgeLabel, HittingSetTreeNode<E> node) {
        edgeLabels.add(edgeLabel);
        children.add(node);
//        tree.incrementSize();
//        System.out.println("Tree size: " + tree.getTreeSize());
    }



    public List<OWLAxiom> getPathToRoot() {
        List<OWLAxiom> path = new ArrayList<OWLAxiom>();
        getPathToRoot(this, path);
        return path;
    }

    private void getPathToRoot(HittingSetTreeNode<E> node, List<OWLAxiom> path) {
        OWLAxiom parentLabel = node.parentEdgeLabel;
        if(parentLabel != null) {
            path.add(0, parentLabel);
            getPathToRoot(node.parentNode, path);
        }
    }


    public Object getParentEdgeLabel() {
        return parentEdgeLabel;
    }
}
