package org.semanticweb.owl.explanation.impl.util;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 02/04/2011
 */
public enum Polarity {

    POSITIVE,
        NEGATIVE;

        public Polarity getReversePolarity() {
            if (this.equals(POSITIVE)) {
                return NEGATIVE;
            }
            else {
                return POSITIVE;
            }
        }

        public boolean isPositive() {
            return this.equals(POSITIVE);
        }
}
