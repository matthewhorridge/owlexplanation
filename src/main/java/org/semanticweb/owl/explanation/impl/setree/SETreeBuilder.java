package org.semanticweb.owl.explanation.impl.setree;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.*;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 19-Jul-2010
 */
public class SETreeBuilder<O> {

    private List<O> elements = new ArrayList<O>();

    private Map<O, Integer> indexMap = new HashMap<O, Integer>();

    public SETreeBuilder(List<O> axioms) {
        this.elements = axioms;
        int index = 1;
        for(O ax : axioms) {
            indexMap.put(ax, index);
            index++;
        }
    }

    public SETreeNode buildTree() {
        SETreeNode<O> root = new SETreeNode<O>(Collections.<O>emptyList());
        extendNode(root);
        return root;
    }

    private void extendNode(SETreeNode<O> node) {
        int maxIndex = getMaxIndex(node);
        for(int i = maxIndex; i < elements.size(); i++) {
            List<O> nodeElements = new ArrayList<O>(node.getElements());
            nodeElements.add(elements.get(i));
            SETreeNode<O> child = new SETreeNode<O>(nodeElements);
            node.addChild(child);
            extendNode(child);
        }
    }

    private int getMaxIndex(SETreeNode<O> node) {
        int maxIndex = 0;
        for(O element : node.getElements()) {
            int index = getIndexOf(element);
            if(index > maxIndex) {
                maxIndex = index;
            }
        }
        return maxIndex;
    }

    public int getIndexOf(O element) {
        return indexMap.get(element);
    }

    private void dump(SETreeNode<O> tree) {
        int depth = tree.getDepth();
        for(int i = 0; i < depth; i++) {
            System.out.print("    ");
        }
        System.out.println(tree);
        for(SETreeNode<O> child : tree.getChildren()) {
            dump(child);
        }
    }


    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21);
        SETreeBuilder<Integer> builder = new SETreeBuilder<Integer>(list);
        SETreeNode root = builder.buildTree();
//        builder.dump(root);
    }

}
