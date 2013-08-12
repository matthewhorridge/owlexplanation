package org.semanticweb.owl.explanation.impl.blackbox.nsp;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owl.explanation.impl.blackbox.EntailmentChecker;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLObjectPropertyManager;

import java.util.HashSet;
import java.util.Set;
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
 * 03-Sep-2008<br><br>
 */
public class NonSimplePropertyEntailmentChecker implements EntailmentChecker<OWLObjectPropertyExpression> {

    private OWLObjectPropertyExpression prop;

    private int counter = 0;

    public NonSimplePropertyEntailmentChecker(OWLObjectPropertyExpression prop) {
        this.prop = prop;
    }


    public int getCounter() {
        return counter;
    }

    public void resetCounter() {
        counter = 0;
    }

    public Set<OWLEntity> getSeedSignature() {
        return prop.getSignature();
    }

    public OWLObjectPropertyExpression getEntailment() {
        return prop;
    }


    public Set<OWLEntity> getEntailmentSignature() {
        return prop.getSignature();
    }


    public boolean isEntailed(Set<OWLAxiom> axioms) {
        try {
            counter++;
            OWLOntologyManager man = OWLManager.createOWLOntologyManager();
            OWLOntology ont = man.createOntology(axioms);
            OWLObjectPropertyManager propman = new OWLObjectPropertyManager(man, ont);
            return propman.isNonSimple(prop);
        }
        catch (OWLOntologyCreationException e) {
            throw new OWLRuntimeException(e);
        }
    }

    public Set<OWLAxiom> getEntailingAxioms(Set<OWLAxiom> axioms) {
        return axioms;
    }

    public Set<OWLAxiom> getModule(Set<OWLAxiom> axioms) {
        Set<OWLAxiom> module = new HashSet<OWLAxiom>(axioms.size());
        for(OWLAxiom ax : axioms) {
            if(ax instanceof OWLSubPropertyAxiom) {
                module.add(ax);
            }
            else if(ax instanceof OWLEquivalentObjectPropertiesAxiom) {
                module.add(ax);
            }
            else if(ax instanceof OWLInverseObjectPropertiesAxiom) {
                module.add(ax);
            }
            else if(ax instanceof OWLSubPropertyChainOfAxiom) {
                module.add(ax);
            }
            else if(ax instanceof OWLTransitiveObjectPropertyAxiom) {
                module.add(ax);
            }
        }
        return module;
    }

    public String getModularisationTypeDescription() {
        return "SubPropertyOf axioms only";
    }

    public boolean isUseModularisation() {
        return true;
    }
}
