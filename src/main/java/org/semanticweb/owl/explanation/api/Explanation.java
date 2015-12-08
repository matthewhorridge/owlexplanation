package org.semanticweb.owl.explanation.api;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationOrderer;
import uk.ac.manchester.cs.owl.explanation.ordering.ExplanationOrdererImpl;

import java.io.*;
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
 * 03-Sep-2008<br><br>
 * Represents an explanation for an entailment.  The explanation consists of a minimal set of axioms (a justification)
 * that is sufficient to support the entailment.  The set is minimal in the sense that any proper subset is not
 * sufficient for the entailment to hold. The type of object that represents the entailment is not specified.  Typically
 * the entailment is an checker, but the entailment could also be another object such as a property that is entailed to
 * be non-simple or transitive, or a set of ontologies that are entailed to be inconsistent for example.
 */
public class Explanation<E> {

    public static final IRI ENTAILMENT_MARKER_IRI = IRI.create("http://owl.cs.manchester.ac.uk/explanation/vocabulary#entailment");

    private E entailment;

    private Set<OWLAxiom> justification;

    /**
     * Gets the entailment that the explanation is for
     * @return The entailment
     */
    public E getEntailment() {
        return entailment;
    }


    /**
     * Gets the axioms (justification) that constitute this explanation.
     * @return A set containing the axioms.
     */
    public Set<OWLAxiom> getAxioms() {
        return justification;
    }

    /**
     * Gets the size of this explanation
     * @return The size which corresponds to the number of axioms in the justification (the entailment is not
     *         counted).
     */
    public int getSize() {
        return justification.size();
    }

    /**
     * Determines if this explanation is empty i.e. there are no axioms that
     * entail the entailment
     * @return <code>true</code> if the explanation is empty or <code>false</code>
     *         if the explanation is not empty.
     */
    public boolean isEmpty() {
        return justification.isEmpty();
    }

    /**
     * Determines whether this explanation contains a specific checker
     * @param axiom The checker
     * @return <code>true</code> if the explanation contains a specific checker
     *         otherwise <code>false</code>
     */
    public boolean contains(OWLAxiom axiom) {
        return justification.contains(axiom);
    }


    /**
     * Determines if the justification for the entailment is the entailment itself
     * @return <code>true</code> if the justification for the entailment is the entailment, otherwise
     *         <code>false</code>
     */
    public boolean isJustificationEntailment() {
        return Collections.singleton(entailment).equals(justification);
    }


    /**
     * Gets the sub-concepts that appear in the axioms in this explanation
     * @return A set of sub-concepts that appear in the axioms in this explanation
     */
    public Set<OWLClassExpression> getNestedClassExpressions() {
        Set<OWLClassExpression> subConcepts = new HashSet<OWLClassExpression>();
        for (OWLAxiom ax : justification) {
            subConcepts.addAll(ax.getNestedClassExpressions());
        }
        return subConcepts;
    }




    public Explanation(E entailment, Set<OWLAxiom> justification) {
        this.entailment = entailment;
        this.justification = Collections.unmodifiableSet(new HashSet<OWLAxiom>(justification));
    }

    public static <E> Explanation<E> getEmptyExplanation(E entailment) {
        Set<OWLAxiom> emptySet = Collections.emptySet();
        return new Explanation<E>(entailment, emptySet);
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (justification.isEmpty()) {
            return "Explanation: <Empty>\n";
        }
        sb.append("Explanation <");
        sb.append(entailment);
        sb.append(">\n");
        Collection<OWLAxiom> orderedAxioms;
        if (entailment instanceof OWLAxiom) {
            OWLAxiom entailedAxiom = (OWLAxiom) entailment;
            ExplanationOrderer orderer = new ExplanationOrdererImpl(OWLManager.createOWLOntologyManager());
            List<OWLAxiom> axs = new ArrayList<OWLAxiom>(orderer.getOrderedExplanation(entailedAxiom, justification).fillDepthFirst());
            axs.remove(0);
            orderedAxioms = axs;
        }
        else {
            orderedAxioms = new TreeSet<OWLAxiom>(justification);
        }

        for (OWLAxiom ax : orderedAxioms) {
            sb.append("\t");
            sb.append(ax);
            sb.append("\n");
        }
        return sb.toString();
    }


    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Explanation)) {
            return false;
        }
        Explanation other = (Explanation) obj;
        return other.getEntailment().equals(entailment) && other.getAxioms().equals(justification);
    }


    public int hashCode() {
        return (entailment != null ? entailment.hashCode() : 0) + justification.hashCode();
    }

    /**
     * Stores the specified explanation to the specified output stream
     * @param explanation The explanation to be stored
     * @param os The output stream to store the explanation to
     * @throws IOException if there was a problem writing out the explanation
     */
    public static void store(Explanation<OWLAxiom> explanation, OutputStream os) throws IOException {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.createOntology(explanation.getAxioms());
            OWLDataFactory df = manager.getOWLDataFactory();
            OWLAnnotationProperty entailmentMarkerAnnotationProperty = df.getOWLAnnotationProperty(ENTAILMENT_MARKER_IRI);
            OWLAnnotation entailmentAnnotation = df.getOWLAnnotation(entailmentMarkerAnnotationProperty, df.getOWLLiteral(true));
            OWLAxiom annotatedEntailment = explanation.getEntailment().getAnnotatedAxiom(Collections.singleton(entailmentAnnotation));
            manager.addAxiom(ontology, annotatedEntailment);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(os);
            OWLXMLDocumentFormat justificationOntologyFormat = new OWLXMLDocumentFormat();
            manager.saveOntology(ontology, justificationOntologyFormat, bufferedOutputStream);
        }
        catch (OWLOntologyStorageException | OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Loads a previously stored explanation from the specified input stream
     * @param is The input stream from where to read the explanation
     * @return The explanation that was read
     * @throws IOException if there was a problem reading the explanation
     * @throws IllegalStateException if the input stream does not appear to contain a serialisation of an explanation.
     */
    public static Explanation<OWLAxiom> load(InputStream is) throws IOException {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new BufferedInputStream(is));
            OWLDataFactory df = manager.getOWLDataFactory();
            OWLAnnotationProperty entailmentMarkerAnnotationProperty = df.getOWLAnnotationProperty(ENTAILMENT_MARKER_IRI);
            Set<OWLAxiom> justificationAxioms = new HashSet<OWLAxiom>();
            OWLAxiom entailment = null;
            for(OWLAxiom ax : ontology.getAxioms()) {
                boolean isEntailmentAxiom = !ax.getAnnotations(entailmentMarkerAnnotationProperty).isEmpty();
                if(!isEntailmentAxiom) {
                    justificationAxioms.add(ax);
                }
                else {
                    entailment = ax.getAxiomWithoutAnnotations();
                }
            }
            if(entailment == null) {
                throw new IllegalStateException("Not a serialisation of an Explanation");
            }
            return new Explanation<OWLAxiom>(entailment, justificationAxioms);
        }
        catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
    }

}
