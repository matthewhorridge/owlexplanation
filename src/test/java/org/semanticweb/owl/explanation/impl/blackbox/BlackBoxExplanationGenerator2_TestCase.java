package org.semanticweb.owl.explanation.impl.blackbox;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.NullExplanationProgressMonitor;
import org.semanticweb.owl.explanation.impl.blackbox.checker.SatisfiabilityEntailmentCheckerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.SubClassOf;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 17/11/15
 */
@RunWith(MockitoJUnitRunner.class)
public class BlackBoxExplanationGenerator2_TestCase {

    private BlackBoxExplanationGenerator2<OWLAxiom> generator;

    private OWLReasonerFactory reasonerFactory;


    private OWLAxiom entailment;

    private OWLAxiom ASubClassOfB;

    private OWLAxiom BSubClassOfC;

    @Before
    public void setUp() throws Exception {

        OWLDataFactory dataFactory = new OWLDataFactoryImpl();
        OWLClass A = dataFactory.getOWLClass(IRI.create("http://example.com/A"));
        OWLClass B = dataFactory.getOWLClass(IRI.create("http://example.com/B"));
        OWLClass C = dataFactory.getOWLClass(IRI.create("http://example.com/C"));


        ASubClassOfB = SubClassOf(A, B);
        BSubClassOfC = SubClassOf(B, C);



        entailment = SubClassOf(A, C);

        reasonerFactory = new PelletReasonerFactory();

        generator = new BlackBoxExplanationGenerator2<>(
                Sets.newHashSet(ASubClassOfB, BSubClassOfC),
                new SatisfiabilityEntailmentCheckerFactory(reasonerFactory),
                new StructuralExpansionStrategy(),
                new SimpleContractionStrategy(),
                new NullExplanationProgressMonitor<OWLAxiom>()
        );

    }

    @Test
    public void test() {
        Set<Explanation<OWLAxiom>> explanations = generator.getExplanations(entailment);
        assertThat(explanations.size(), is(1));
        Explanation<OWLAxiom> explanation = explanations.iterator().next();
        assertThat(explanation.getEntailment(), is(entailment));
        assertThat(explanation.getAxioms().size(), is(2));
        assertThat(explanation.getAxioms(), containsInAnyOrder(ASubClassOfB, BSubClassOfC));
    }

}
