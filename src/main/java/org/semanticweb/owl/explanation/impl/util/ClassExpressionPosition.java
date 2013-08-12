package org.semanticweb.owl.explanation.impl.util;

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 14/02/2011
 */
public class ClassExpressionPosition {

    private OWLClassExpression classExpression;

    private Position position;

    public ClassExpressionPosition(OWLClassExpression classExpression, Position position) {
        this.classExpression = classExpression;
        this.position = position;
    }

    public OWLClassExpression getClassExpression() {
        return classExpression;
    }

    public Position getPosition() {
        return position;
    }
}
