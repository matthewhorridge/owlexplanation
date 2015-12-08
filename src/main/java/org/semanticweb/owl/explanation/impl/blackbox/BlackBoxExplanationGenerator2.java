package org.semanticweb.owl.explanation.impl.blackbox;

import org.semanticweb.owl.explanation.api.*;
import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.impl.blackbox.hst.*;
import org.semanticweb.owl.explanation.telemetry.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.TimeOutException;

import java.util.*;
/*
 * Copyright (C) 2010, University of Manchester
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
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Information Management Group<br>
 * Date: 18-Feb-2010
 */
public class BlackBoxExplanationGenerator2<E> implements ExplanationGenerator<E>, ExplanationGeneratorMediator<E> {


    private ExpansionStrategy expansionStrategy;

    private ContractionStrategy contractionStrategy;

    private EntailmentCheckerFactory<E> checkerFactory;

    private Set<OWLAxiom> workingAxioms;

    private Set<OWLAxiom> module;

    private ExplanationProgressMonitor<E> progressMonitor;

    private Set<Explanation<E>> cache = new HashSet<>();

    private TelemetryTimer generatorTimer = new TelemetryTimer();
//    private TelemetryTimer findOneElapsedTimer;

    /**
     * Constructs a blackbox explanation generator.
     * @param axioms The ontologies that provide the source axioms for the explanation
     * @param checkerFactory A factory that creates the appropriate entailment checkers for the
     * type of entailment being explained.
     * @param expansionStrategy The strategy used during the expansion phase
     * @param contractionStrategy The strategy to be used during the contraction phase
     * @param progressMonitor A progress monitor - may be <code>null</code>
     */
    public BlackBoxExplanationGenerator2(Set<? extends OWLAxiom> axioms, EntailmentCheckerFactory<E> checkerFactory, ExpansionStrategy expansionStrategy, ContractionStrategy contractionStrategy, ExplanationProgressMonitor<E> progressMonitor) {
        workingAxioms = new HashSet<OWLAxiom>(axioms);
        this.checkerFactory = checkerFactory;
        this.expansionStrategy = expansionStrategy;
        this.contractionStrategy = contractionStrategy;
        if (progressMonitor != null) {
            this.progressMonitor = progressMonitor;
        }
        else {
            this.progressMonitor = new NullExplanationProgressMonitor<E>();
        }
//        findOneElapsedTimer = new TelemetryTimer();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Implementation of interfaces
    //
    //////////////////////////////////////////////////////////////////////////////////////////


    public Set<Explanation<E>> getExplanations(E entailment) throws ExplanationException {
        return getExplanations(entailment, Integer.MAX_VALUE);
    }


    public Set<Explanation<E>> getExplanations(E entailment, int limit) throws ExplanationException {
        TelemetryInfo justificationsInfo = new DefaultTelemetryInfo("justifications");
        TelemetryTransmitter transmitter = TelemetryTransmitter.getTransmitter();

        try {
            cache.clear();
            transmitter.beginTransmission(justificationsInfo);
            transmitter.recordMeasurement(justificationsInfo, "entailment", entailment.toString());
            transmitter.recordMeasurement(justificationsInfo, "input size", workingAxioms.size());

            EntailmentChecker<E> checker = checkerFactory.createEntailementChecker(entailment);

            extractModule(checker);


            generatorTimer.reset();
            generatorTimer.start();
            HittingSetTreeConstructionStrategy<E> strategy = new BreadthFirstStrategy<E>();
            HittingSetTree<E> hittingSetTree = new HittingSetTree<E>(strategy, progressMonitor);
            hittingSetTree.buildHittingSetTree(entailment, limit, this);
            return hittingSetTree.getExplanations();
        }
        finally {
            transmitter.endTransmission(justificationsInfo);
        }
    }

    private void extractModule(EntailmentChecker<E> checker) {
        TelemetryTransmitter transmitter = TelemetryTransmitter.getTransmitter();
        TelemetryTimer modularisationTimer = new TelemetryTimer();
        TelemetryInfo moduleInfo = new DefaultTelemetryInfo("module", modularisationTimer);
        try {
            transmitter.beginTransmission(moduleInfo);
            modularisationTimer.start();
            module = extractModule(workingAxioms, checker);
            modularisationTimer.stop();
        }
        finally {
            transmitter.recordMeasurement(moduleInfo, "module type", checker.getModularisationTypeDescription());
            transmitter.recordMeasurement(moduleInfo, "input size", workingAxioms.size());
            if (module != null) {
                transmitter.recordMeasurement(moduleInfo, "module size", module.size());
            }
            transmitter.recordTiming(moduleInfo, "module extraction time", modularisationTimer);
            transmitter.endTransmission(moduleInfo);
        }
    }

    public Set<OWLAxiom> getWorkingAxioms() {
        return workingAxioms;
    }


    /**
     * Computes a single justification for an entailment.
     * @param entailment The entailment
     * @return The justification or an empty set if the entailment does not hold.
     */
    protected Explanation<E> computeExplanation(E entailment) {

        TelemetryTimer justificationTimer = new TelemetryTimer();

        TelemetryInfo findOneInfo = new DefaultTelemetryInfo("findone", justificationTimer);
        final TelemetryTransmitter transmitter = TelemetryTransmitter.getTransmitter();
        transmitter.beginTransmission(findOneInfo);

        Explanation<E> result = Explanation.getEmptyExplanation(entailment);

        boolean entailed = false;

        try {
            justificationTimer.start();

            // Quick get out!
            if (entailment instanceof OWLAxiom && workingAxioms.contains((OWLAxiom) entailment)) {
                entailed = true;
                result = new Explanation<E>(entailment, Collections.singleton((OWLAxiom) entailment));
            }
            else {
                if (progressMonitor.isCancelled()) {
                    throw new ExplanationGeneratorInterruptedException();
                }
                EntailmentChecker<E> checker = checkerFactory.createEntailementChecker(entailment);
                // Expansion phase
                Set<OWLAxiom> expandedAxioms = doExpansion(checker);

                if (!expandedAxioms.isEmpty()) {
                    // Contraction phase
                    Set<OWLAxiom> justificationAxioms = doContraction(checker, expandedAxioms);

                    result = new Explanation<E>(entailment, justificationAxioms);
                }

            }
            justificationTimer.stop();

        }
        catch (TimeOutException e) {
            transmitter.recordMeasurement(findOneInfo, "reasoner time out", true);
            throw e;
        }
        catch (ExplanationGeneratorInterruptedException e) {
            transmitter.recordMeasurement(findOneInfo, "interrupted", true);
            throw e;
        }
        catch (RuntimeException e) {
            transmitter.recordException(findOneInfo, e);
            throw e;
        }
        finally {
            transmitter.recordMeasurement(findOneInfo, "input size", module.size());
            transmitter.recordMeasurement(findOneInfo, "entailed", entailed);
            transmitter.recordMeasurement(findOneInfo, "self justification", result.isJustificationEntailment());
            transmitter.recordMeasurement(findOneInfo, "justification size", result.getSize());
            transmitter.recordTiming(findOneInfo, "time", justificationTimer);
            recordJustification(entailment, findOneInfo, result);
            transmitter.endTransmission(findOneInfo);
        }


        return result;
    }

    private void recordJustification(E entailment, TelemetryInfo findOneInfo, Explanation<E> result) {
        TelemetryTransmitter transmitter = TelemetryTransmitter.getTransmitter();
        if (!result.isEmpty()) {
            if (entailment instanceof OWLAxiom) {
                ExplanationTelemetryWrapper telemetryObject = new ExplanationTelemetryWrapper((Explanation<OWLAxiom>) result);
                transmitter.recordObject(findOneInfo, "justification", ".owl.xml", telemetryObject);
            }
        }
    }

    private Set<OWLAxiom> doContraction(EntailmentChecker<E> checker, Set<OWLAxiom> expandedAxioms) {
        TelemetryTransmitter transmitter = TelemetryTransmitter.getTransmitter();
        TelemetryTimer contractionTimer = new TelemetryTimer();
        TelemetryInfo contractionInfo = new DefaultTelemetryInfo("contraction", contractionTimer);
        Set<OWLAxiom> justificationAxioms;
        try {
            contractionTimer.start();
            transmitter.beginTransmission(contractionInfo);
            justificationAxioms = contractionStrategy.doPruning(expandedAxioms, checker, progressMonitor);
            contractionTimer.stop();
            transmitter.recordMeasurement(contractionInfo, "contraction strategy", contractionStrategy.getClass().getName());
            transmitter.recordMeasurement(contractionInfo, "contraction size", justificationAxioms.size());
            transmitter.recordTiming(contractionInfo, "contraction time", contractionTimer);
            transmitter.recordMeasurement(contractionInfo, "contraction entailment check count", contractionStrategy.getNumberOfSteps());

        }
        finally {
            transmitter.endTransmission(contractionInfo);
        }
        return justificationAxioms;
    }

    private Set<OWLAxiom> doExpansion(EntailmentChecker<E> checker) {
        TelemetryTransmitter transmitter = TelemetryTransmitter.getTransmitter();
        TelemetryTimer expansionTimer = new TelemetryTimer();
        TelemetryInfo expansionInfo = new DefaultTelemetryInfo("expansion", expansionTimer);
        Set<OWLAxiom> expandedAxioms;
        try {
            transmitter.beginTransmission(expansionInfo);
            expansionTimer.start();
            expandedAxioms = expansionStrategy.doExpansion(module, checker, progressMonitor);
            expansionTimer.stop();
            transmitter.recordMeasurement(expansionInfo, "expansion strategy", expansionStrategy.getClass().getName());
            transmitter.recordMeasurement(expansionInfo, "expansion size", expandedAxioms.size());
            transmitter.recordTiming(expansionInfo, "expansion time", expansionTimer);
            transmitter.recordMeasurement(expansionInfo, "expansion entailment check count", expansionStrategy.getNumberOfSteps());
        }
        finally {
            transmitter.endTransmission(expansionInfo);
        }
        return expandedAxioms;
    }


    protected Set<OWLAxiom> extractModule(Set<OWLAxiom> axioms, EntailmentChecker<E> checker) {
        return checker.getModule(axioms);
    }


    private int cacheHitCounter = 0;

    public Explanation<E> generateExplanation(E entailment) {
        for (Explanation<E> expl : cache) {
            if (expl.getEntailment().equals(entailment)) {
                if (module.containsAll(expl.getAxioms())) {
                    cacheHitCounter++;
                    return expl;
                }
            }
        }

        Explanation<E> expl = computeExplanation(entailment);
        if (!expl.isEmpty()) {
            cache.add(expl);
        }
        return expl;
    }

    public void removeAxiom(OWLAxiom axiom) {
        module.remove(axiom);
        workingAxioms.remove(axiom);
    }

    public void addAxiom(OWLAxiom axiom) {
        module.add(axiom);
        workingAxioms.add(axiom);
    }


}
