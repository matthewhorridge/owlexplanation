package org.semanticweb.owl.explanation.impl.laconic;

import org.semanticweb.owl.explanation.api.*;
import org.semanticweb.owl.explanation.impl.blackbox.EntailmentCheckerFactory;
import org.semanticweb.owl.explanation.impl.util.AxiomTransformation;
import org.semanticweb.owl.explanation.impl.util.DeltaPlusTransformation;
import org.semanticweb.owl.explanation.impl.util.DeltaTransformation;
import org.semanticweb.owl.explanation.impl.util.DeltaTransformationUnfolder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 11/04/2011
 */
public class LaconicExplanationGeneratorBasedOnDeltaPlus implements ExplanationGenerator<OWLAxiom> {

    private Set<OWLAxiom> inputAxioms;

    private EntailmentCheckerFactory<OWLAxiom> entailmentCheckerFactory;

    private ExplanationGeneratorFactory<OWLAxiom> delegateFactory;

    private ExplanationProgressMonitor<OWLAxiom> progressMonitor;

    public LaconicExplanationGeneratorBasedOnDeltaPlus(Set<? extends OWLAxiom> inputAxioms, EntailmentCheckerFactory<OWLAxiom> entailmentCheckerFactory, ExplanationGeneratorFactory<OWLAxiom> delegateFactory, ExplanationProgressMonitor<OWLAxiom> progressMonitor) {
        this.inputAxioms = new HashSet<OWLAxiom>(inputAxioms);
        this.entailmentCheckerFactory = entailmentCheckerFactory;
        this.delegateFactory = delegateFactory;
        this.progressMonitor = progressMonitor;
    }

    /**
     * Gets explanations for an entailment.  All explanations for the entailment will be returned.
     * @param entailment The entailment for which explanations will be generated.
     * @return A set containing all of the explanations.  The set will be empty if the entailment does not hold.
     * @throws org.semanticweb.owl.explanation.api.ExplanationException
     *          if there was a problem generating the explanation.
     */
    public Set<Explanation<OWLAxiom>> getExplanations(OWLAxiom entailment) throws ExplanationException {
        return getExplanations(entailment, Integer.MAX_VALUE);
    }

    /**
     * Gets explanations for an entailment, with limit on the number of explanations returned.
     * @param entailment The entailment for which explanations will be generated.
     * @param limit The maximum number of explanations to generate. This should be a positive integer.
     * @return A set containing explanations.  The maximum cardinality of the set is specified by the limit parameter.
     *         The set may be empty if the entailment does not hold, or if a limit of zero or less is supplied.
     * @throws org.semanticweb.owl.explanation.api.ExplanationException
     *          if there was a problem generating the explanation.
     */
    public Set<Explanation<OWLAxiom>> getExplanations(OWLAxiom entailment, int limit) throws ExplanationException {
        final Set<OWLEntity> signature = new HashSet<OWLEntity>();
        for(OWLAxiom ax : inputAxioms) {
            signature.addAll(ax.getSignature());
        }
        final OWLDataFactory dataFactory = OWLDataFactoryImpl.getInstance();
        AxiomTransformation transformation = new DeltaPlusTransformation(dataFactory);
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(man, null, inputAxioms, ModuleType.STAR);
        Set<OWLAxiom> moduleAxioms = extractor.extract(entailment.getSignature());
//
//        ExplanationGenerator<OWLAxiom> regGen = delegateFactory.createExplanationGenerator(inputAxioms, new NullExplanationProgressMonitor<OWLAxiom>());
//        Set<Explanation<OWLAxiom>> regexpls = regGen.getExplanations(entailment);
//        System.out.println(".........................................................");

        Set<OWLAxiom> flattenedAxioms = transformation.transform(moduleAxioms);
        ExplanationGenerator<OWLAxiom> gen = delegateFactory.createExplanationGenerator(flattenedAxioms);
        Set<Explanation<OWLAxiom>> expls = gen.getExplanations(entailment);
//        System.out.println("Found " + expls.size() + " regular expls");
//        for(Explanation<OWLAxiom> expl : expls) {
//            System.out.println(expl);
//        }
//        if(expls.isEmpty()) {
//        {
//            System.out.println("Not found any delta plus justifications!");
//            for(OWLAxiom ax : flattenedAxioms) {
//                System.out.println(ax);
//            }
//            System.out.println("........................................................");
//            DeltaTransformationUnfolder unfolder = new DeltaTransformationUnfolder(dataFactory);
//            Set<OWLAxiom> unfoldedAxioms = unfolder.getUnfolded(flattenedAxioms, signature);
//            for(OWLAxiom ax : unfoldedAxioms) {
//                System.out.println(ax);
//            }
//        }

        IsLaconicChecker checker = new IsLaconicChecker(dataFactory, entailmentCheckerFactory, LaconicCheckerMode.EARLY_TERMINATING);
        Set<Explanation<OWLAxiom>> laconicExplanations = new HashSet<Explanation<OWLAxiom>>();
        Set<Explanation<OWLAxiom>> nonLaconicExplanations = new HashSet<Explanation<OWLAxiom>>();
        for(Explanation<OWLAxiom> expl : expls) {
            DeltaTransformationUnfolder unfolder = new DeltaTransformationUnfolder(dataFactory);
            Set<OWLAxiom> unfoldedAxioms = unfolder.getUnfolded(expl.getAxioms(), signature);
            Explanation<OWLAxiom> unfoldedExpl = new Explanation<OWLAxiom>(entailment, unfoldedAxioms);
            if(checker.isLaconic(unfoldedExpl)) {
                boolean added = laconicExplanations.add(unfoldedExpl);
                if (added) {
                    progressMonitor.foundExplanation(this, unfoldedExpl, new HashSet<Explanation<OWLAxiom>>(laconicExplanations));
                }
            }
            else {
                nonLaconicExplanations.add(unfoldedExpl);
            }
        }
        if(laconicExplanations.isEmpty()) {
            System.out.println("NOT-FOUND-ANY!");
            for(Explanation<OWLAxiom> nonLaconic : nonLaconicExplanations) {
                System.out.println("NON-LACONIC:");
                System.out.println(nonLaconic);
            }
        }
        return laconicExplanations;

    }




}
