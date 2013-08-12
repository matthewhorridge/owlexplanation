package org.semanticweb.owl.explanation.impl.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 14/02/2011
 */
public class Position {

    private List<Integer> positionList = new ArrayList<Integer>();

    public Position(Integer ... position) {
        positionList.addAll(Arrays.<Integer>asList(position));
    }

    public Position(List<Integer> position, Integer childPosition) {
        positionList.addAll(position);
        positionList.add(childPosition);
    }

    public Position(Integer position) {
        positionList.add(position);
    }

    public Position() {
    }

    public boolean isEmpty() {
        return positionList.isEmpty();
    }

    public Position addPosition(Integer position) {
        return new Position(positionList, position);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(Iterator<Integer> positionIterator = positionList.iterator(); positionIterator.hasNext(); ) {
            Integer currentPosition = positionIterator.next();
            sb.append(currentPosition);
            if(positionIterator.hasNext()) {
                sb.append(".");
            }
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return positionList.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(!(obj instanceof Position)) {
            return false;
        }
        Position otherPosition = (Position) obj;
        return positionList.equals(otherPosition.positionList);
    }
}
