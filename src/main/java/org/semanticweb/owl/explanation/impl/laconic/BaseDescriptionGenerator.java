package org.semanticweb.owl.explanation.impl.laconic;

import org.semanticweb.owlapi.model.*;

import java.util.*;
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
 * 15-Sep-2008<br><br>
 */
public abstract class BaseDescriptionGenerator implements OWLClassExpressionVisitorEx<Set<OWLClassExpression>> {

    private OWLDataFactory factory;

    private static TriviallyTopChecker topChecker = new TriviallyTopChecker();

    private static TriviallyBottomChecker bottomChecker = new TriviallyBottomChecker();


    public BaseDescriptionGenerator(OWLDataFactory factory) {
        this.factory = factory;
    }

    public boolean isThing(OWLClassExpression description) {
        return description.accept(topChecker);
    }

    public boolean isNothing(OWLClassExpression description) {
        return description.accept(bottomChecker);
    }

    public OWLDataFactory getDataFactory() {
        return factory;
    }


    public Set<OWLClassExpression> computeTau(OWLClassExpression desc) {
        TauGenerator gen = new TauGenerator(factory);
        return desc.accept(gen);
    }


    public Set<OWLClassExpression> computeBeta(OWLClassExpression desc) {
        BetaGenerator gen = new BetaGenerator(factory);
        return desc.accept(gen);
    }

    private Set<Set<OWLClassExpression>> computeReplacements(Set<OWLClassExpression> operands) {
        Set<List<OWLClassExpression>> ps = new HashSet<List<OWLClassExpression>>();
        ps.add(new ArrayList<OWLClassExpression>());
        for (OWLClassExpression op : operands) {
            Set<List<OWLClassExpression>> pscopy = new HashSet<List<OWLClassExpression>>(ps);

            for (OWLClassExpression opEx : op.accept(this)) {
                for (List<OWLClassExpression> pselement : pscopy) {
                    ArrayList<OWLClassExpression> union = new ArrayList<OWLClassExpression>();

                    union.addAll(pselement);
                    union.add(opEx);
                    ps.remove(pselement);
                    ps.add(union);
                }
            }
        }
        Set<Set<OWLClassExpression>> result = new HashSet<Set<OWLClassExpression>>();
        for(List<OWLClassExpression> desc : ps) {
            result.add(new HashSet<OWLClassExpression>(desc));
        }
        return result;
    }



    public Set<OWLClassExpression> visit(OWLObjectIntersectionOf desc) {
        Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
        Set<Set<OWLClassExpression>> conjunctions = computeReplacements(desc.getOperands());
        for (Set<OWLClassExpression> conjuncts : conjunctions) {
            for(Iterator<OWLClassExpression> it = conjuncts.iterator(); it.hasNext(); ) {
                if(isThing(it.next())) {
                    it.remove();
                }
            }
            if(conjuncts.isEmpty()) {
                descs.add(factory.getOWLThing());
            }
            else if (conjuncts.size() != 1) {
                descs.add(factory.getOWLObjectIntersectionOf(conjuncts));
            }
            else {
//                descs.add(factory.getOWLObjectIntersectionOf(conjuncts));
                descs.add(conjuncts.iterator().next());
            }
        }
        descs.add(getLimit());
        return descs;
    }


    public Set<OWLClassExpression> visit(OWLObjectUnionOf desc) {
        Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
        Set<Set<OWLClassExpression>> disjunctions = computeReplacements(desc.getOperands());
        for (Set<OWLClassExpression> disjuncts : disjunctions) {
            for(Iterator<OWLClassExpression> it = disjuncts.iterator(); it.hasNext(); ) {
                if(isNothing(it.next())) {
                    it.remove();
                }
            }
            if (disjuncts.size() != 1) {
                descs.add(factory.getOWLObjectUnionOf(disjuncts));
            }
            else {
                descs.add(disjuncts.iterator().next());
            }
        }
        descs.add(getLimit());
        return descs;
    }


    public Set<OWLClassExpression> visit(OWLObjectSomeValuesFrom desc) {
        Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
        descs.add(desc);
        for (OWLClassExpression filler : desc.getFiller().accept(this)) {
            if (!isNothing(filler)) {
                descs.add(factory.getOWLObjectSomeValuesFrom(desc.getProperty(), filler));
            }
        }
        descs.add(getLimit());
        return descs;
    }


    public Set<OWLClassExpression> visit(OWLObjectAllValuesFrom desc) {
        Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
        for (OWLClassExpression filler : desc.getFiller().accept(this)) {
            if (!isThing(filler)) {
                descs.add(factory.getOWLObjectAllValuesFrom(desc.getProperty(), filler));
            }
        }
        descs.add(getLimit());
        return descs;
    }


    public Set<OWLClassExpression> visit(OWLObjectHasValue desc) {
        Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
        descs.add(desc);
//        for (OWLClassExpression filler : factory.getOWLObjectOneOf(desc.getValue()).accept(this)) {
//            descs.add(factory.getOWLObjectSomeValuesFrom(desc.getProperty(), filler));
//        }
        descs.add(factory.getOWLObjectSomeValuesFrom(desc.getProperty(), factory.getOWLThing()));
        descs.add(getLimit());
        return descs;
    }

    public Set<OWLClassExpression> visit(OWLObjectExactCardinality desc) {
        // Syntactic for min and max
        Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
        OWLClassExpression min = getDataFactory().getOWLObjectMinCardinality(desc.getCardinality(), desc.getProperty(), desc.getFiller());
        result.addAll(min.accept(this));
        OWLClassExpression max = getDataFactory().getOWLObjectMaxCardinality(desc.getCardinality(), desc.getProperty(), desc.getFiller());
        result.addAll(max.accept(this));
        result.add(getLimit());
        return result;
    }

    public Set<OWLClassExpression> visit(OWLObjectHasSelf desc) {
        Set<OWLClassExpression> descs = new HashSet<OWLClassExpression>();
        descs.add(desc);
        descs.add(getLimit());
        return descs;
    }


    public Set<OWLClassExpression> visit(OWLObjectOneOf desc) {
        Set<OWLClassExpression> ops = new HashSet<OWLClassExpression>();
        if(desc.getIndividuals().size() == 1) {
            ops.add(desc);
            ops.add(getLimit());
            return ops;
        }
        for (OWLIndividual ind : desc.getIndividuals()) {
            ops.add(factory.getOWLObjectOneOf(ind));
        }
        OWLClassExpression rewrite = factory.getOWLObjectUnionOf(ops);
        return rewrite.accept(this);
    }

    protected abstract OWLClass getLimit();

    protected abstract OWLDataRange getDataLimit();

    public Set<OWLClassExpression> visit(OWLDataSomeValuesFrom desc) {
//        Set<OWLClassExpression> result = new HashSet<OWLClassExpression>();
//        result.add(desc);
//        result.add(factory.getOWLDataSomeValuesFrom(desc.getProperty(), factory.getTopDatatype()));
//        return result;
        return Collections.<OWLClassExpression>singleton(desc);
    }


    public Set<OWLClassExpression> visit(OWLDataAllValuesFrom desc) {
        return Collections.singleton((OWLClassExpression) desc);
    }


    public Set<OWLClassExpression> visit(OWLDataHasValue desc) {
//        Set<OWLClassExpression> result = new HashSet<OWLClassExpression>(2);
//        result.add(desc);
//        result.add(getDataFactory().getOWLDataSomeValuesFrom(desc.getProperty(),  getDataLimit()));
//        return result;
        return Collections.<OWLClassExpression>singleton(desc);
    }


    public Set<OWLClassExpression> visit(OWLDataMinCardinality desc) {
        return Collections.singleton((OWLClassExpression) desc);
    }


    public Set<OWLClassExpression> visit(OWLDataExactCardinality desc) {
        return Collections.singleton((OWLClassExpression) desc);
    }


    public Set<OWLClassExpression> visit(OWLDataMaxCardinality desc) {
        return Collections.singleton((OWLClassExpression) desc);
    }
}
