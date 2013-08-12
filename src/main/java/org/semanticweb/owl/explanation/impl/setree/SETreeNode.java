package org.semanticweb.owl.explanation.impl.setree;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 19-Jul-2010
 */
public class SETreeNode<O> {

    private List<O> elements;

    private SETreeNode<O> parent;

    private List<SETreeNode> children = new ArrayList<SETreeNode>();

    public SETreeNode(List<O> elements) {
        this.elements = elements;
    }

    public List<O> getElements() {
        return elements;
    }

    public void addChild(SETreeNode node) {
        children.add(node);
        node.parent = this;
    }

    public List<SETreeNode> getChildren() {
        return children;
    }

    public int getDepth() {
        SETreeNode<O> current = this.parent;
        int depth = 0;
        while(current != null) {
            current = current.parent;
            depth++;
        }
        return depth;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        for(O element : elements) {
            sb.append(element);
            sb.append(" ");
        }
        sb.append("}");
        return sb.toString();
    }
}
