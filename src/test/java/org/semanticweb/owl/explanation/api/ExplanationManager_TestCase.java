package org.semanticweb.owl.explanation.api;

import openllet.owlapi.OpenlletReasonerFactory;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ExplanationManager_TestCase {

    @Test
    public void shouldCreateExplanationUsingDefaultFactory() {
        OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
        OWLClass a = dataFactory.getOWLClass(IRI.create("urn:test#A"));
        OWLClass b = dataFactory.getOWLClass(IRI.create("urn:test#B"));
        OWLClass c = dataFactory.getOWLClass(IRI.create("urn:test#C"));
        OWLAxiom aSubClassOfB = dataFactory.getOWLSubClassOfAxiom(a, b);
        OWLAxiom bSubClassOfC = dataFactory.getOWLSubClassOfAxiom(b, c);
        OWLAxiom entailment = dataFactory.getOWLSubClassOfAxiom(a, c);
        Set<OWLAxiom> axioms = new HashSet<>();
        axioms.add(aSubClassOfB);
        axioms.add(bSubClassOfC);

        ExplanationGenerator<OWLAxiom> generator = ExplanationManager
                .createExplanationGeneratorFactory(
                        new OpenlletReasonerFactory(),
                        OWLManager::createOWLOntologyManager)
                .createExplanationGenerator(axioms);

        Set<Explanation<OWLAxiom>> explanations = generator.getExplanations(entailment, 1);

        assertEquals(1, explanations.size());
        assertEquals(axioms, explanations.iterator().next().getAxioms());
    }
}
