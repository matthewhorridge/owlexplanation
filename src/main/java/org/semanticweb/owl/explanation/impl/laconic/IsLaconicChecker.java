package org.semanticweb.owl.explanation.impl.laconic;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.impl.blackbox.EntailmentChecker;
import org.semanticweb.owl.explanation.impl.blackbox.EntailmentCheckerFactory;
import org.semanticweb.owl.explanation.impl.util.DeltaTransformation;
import org.semanticweb.owl.explanation.telemetry.DefaultTelemetryInfo;
import org.semanticweb.owl.explanation.telemetry.TelemetryInfo;
import org.semanticweb.owl.explanation.telemetry.TelemetryTransmitter;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 02/04/2011
 */
public class IsLaconicChecker {

    private OWLDataFactory dataFactory;

    private EntailmentCheckerFactory<OWLAxiom> entailmentCheckerFactory;


    private LaconicCheckerMode checkerMode;

    private DeltaTransformation deltaTransformation;

    private Set<OWLAnnotation> nonLaconicSourceAxioms = new HashSet<OWLAnnotation>();

    public IsLaconicChecker(OWLDataFactory dataFactory, EntailmentCheckerFactory<OWLAxiom> entailmentCheckerFactory) {
        this(dataFactory, entailmentCheckerFactory, LaconicCheckerMode.EARLY_TERMINATING);
    }

    public IsLaconicChecker(OWLDataFactory dataFactory, EntailmentCheckerFactory<OWLAxiom> entailmentCheckerFactory, LaconicCheckerMode checkerMode) {
        this.dataFactory = dataFactory;
        this.entailmentCheckerFactory = entailmentCheckerFactory;
        this.checkerMode = checkerMode;
    }

    public boolean isLaconic(Explanation<OWLAxiom> expl) {
        if(expl.isJustificationEntailment()) {
            return true;
        }

        TelemetryTransmitter transmitter = TelemetryTransmitter.getTransmitter();
        TelemetryInfo info = new DefaultTelemetryInfo("laconiccheck");
        transmitter.beginTransmission(info);

        boolean laconic = true;
        int entailmentCheckCount = 0;
        nonLaconicSourceAxioms.clear();

        try {


            Set<OWLAxiom> flattened = getFlattenedAxioms(expl.getAxioms());


            transmitter.recordMeasurement(info, "justification size", expl.getSize());
            transmitter.recordMeasurement(info, "delta axioms size", flattened.size());

            EntailmentChecker<OWLAxiom> checker = entailmentCheckerFactory.createEntailementChecker(expl.getEntailment());

            for (OWLAxiom curAxiom : new ArrayList<OWLAxiom>(flattened)) {
                flattened.remove(curAxiom);
                entailmentCheckCount++;
                if (checker.isEntailed(flattened)) {
                    laconic = false;
                    recordNonLaconicity(info, curAxiom);
                    if(checkerMode.equals(LaconicCheckerMode.EARLY_TERMINATING)) {
                        return false;
                    }
                }
                flattened.add(curAxiom);
                if (curAxiom instanceof OWLSubClassOfAxiom) {
                    OWLSubClassOfAxiom sca = (OWLSubClassOfAxiom) curAxiom;
                    if (sca.getSubClass() instanceof OWLObjectMinCardinality) {
                        entailmentCheckCount++;
                        if (isEntailedByWeakerMinCardinalitySubClass(sca, flattened, checker)) {
                            laconic = false;
                            recordNonLaconicity(info, curAxiom);
                            if(checkerMode.equals(LaconicCheckerMode.EARLY_TERMINATING)) {
                                return false;
                            }
                        }
                    }
                    else if (sca.getSubClass() instanceof OWLObjectMaxCardinality) {
                        entailmentCheckCount++;
                        if (isEntailedByWeakerMaxCardinalitySubClass(sca, flattened, checker)) {
                            laconic = false;
                            recordNonLaconicity(info, curAxiom);
                            if(checkerMode.equals(LaconicCheckerMode.EARLY_TERMINATING)) {
                                return false;
                            }
                        }
                    }
                    else if (sca.getSuperClass() instanceof OWLObjectMinCardinality) {
                        entailmentCheckCount++;
                        if (isEntailedByWeakerMinCardinalitySuperClass(sca, flattened, checker)) {
                            laconic = false;
                            recordNonLaconicity(info, curAxiom);
                            if(checkerMode.equals(LaconicCheckerMode.EARLY_TERMINATING)) {
                                return false;
                            }
                        }
                    }
                    else if (sca.getSuperClass() instanceof OWLObjectMaxCardinality) {
                        entailmentCheckCount++;
                        if (isEntailedByWeakerMaxCardinalitySuperClass(sca, flattened, checker)) {
                            laconic = false;
                            recordNonLaconicity(info, curAxiom);
                            if(checkerMode.equals(LaconicCheckerMode.EARLY_TERMINATING)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        finally {
            transmitter.recordMeasurement(info, "laconic", laconic);
            transmitter.recordMeasurement(info, "entailment check count", entailmentCheckCount);
            transmitter.recordMeasurement(info, "number of non-laconic source axioms", nonLaconicSourceAxioms.size());
            transmitter.endTransmission(info);
        }
        return laconic;
    }

    private void recordNonLaconicity(TelemetryInfo info, OWLAxiom curAxiom) {
        TelemetryTransmitter transmitter = TelemetryTransmitter.getTransmitter();
        nonLaconicSourceAxioms.addAll(curAxiom.getAnnotations());
        int modalDepth = deltaTransformation.getModalDepth(curAxiom);
        transmitter.recordMeasurement(info, "superfluity depth", modalDepth);
    }

    private boolean isEntailedByWeakerMinCardinalitySubClass(OWLSubClassOfAxiom ax, Set<OWLAxiom> axioms, EntailmentChecker<OWLAxiom> checker) {
        OWLObjectMinCardinality cardinality = (OWLObjectMinCardinality) ax.getSubClass();
        int card = cardinality.getCardinality();
        OWLObjectPropertyExpression prop = cardinality.getProperty();
        OWLClassExpression ce = cardinality.getFiller();
        OWLObjectMinCardinality weaker = dataFactory.getOWLObjectMinCardinality(card + 1, prop, ce);
        OWLAxiom weakerAxiom = dataFactory.getOWLSubClassOfAxiom(weaker, ax.getSuperClass());
        return isEntailedWithReplacement(ax, weakerAxiom, axioms, checker);
    }

    private boolean isEntailedByWeakerMaxCardinalitySubClass(OWLSubClassOfAxiom ax, Set<OWLAxiom> axioms, EntailmentChecker<OWLAxiom> checker) {
        OWLObjectMaxCardinality cardinality = (OWLObjectMaxCardinality) ax.getSubClass();
        int card = cardinality.getCardinality();
        if (card == 0) {
            return false;
        }
        OWLObjectPropertyExpression prop = cardinality.getProperty();
        OWLClassExpression ce = cardinality.getFiller();
        OWLObjectMaxCardinality weaker = dataFactory.getOWLObjectMaxCardinality(card - 1, prop, ce);
        OWLAxiom weakerAxiom = dataFactory.getOWLSubClassOfAxiom(weaker, ax.getSuperClass());
        return isEntailedWithReplacement(ax, weakerAxiom, axioms, checker);
    }

    private boolean isEntailedByWeakerMinCardinalitySuperClass(OWLSubClassOfAxiom ax, Set<OWLAxiom> axioms, EntailmentChecker<OWLAxiom> checker) {
        OWLObjectMinCardinality cardinality = (OWLObjectMinCardinality) ax.getSuperClass();
        int card = cardinality.getCardinality();
        if (card == 1 || card == 0) {
            return false;
        }
        OWLObjectPropertyExpression prop = cardinality.getProperty();
        OWLClassExpression ce = cardinality.getFiller();
        OWLObjectMinCardinality weaker = dataFactory.getOWLObjectMinCardinality(card - 1, prop, ce);
        OWLAxiom weakerAxiom = dataFactory.getOWLSubClassOfAxiom(ax.getSubClass(), weaker);
        return isEntailedWithReplacement(ax, weakerAxiom, axioms, checker);
    }

    private boolean isEntailedByWeakerMaxCardinalitySuperClass(OWLSubClassOfAxiom ax, Set<OWLAxiom> axioms, EntailmentChecker<OWLAxiom> checker) {
        OWLObjectMaxCardinality cardinality = (OWLObjectMaxCardinality) ax.getSuperClass();
        int card = cardinality.getCardinality();
        OWLObjectPropertyExpression prop = cardinality.getProperty();
        OWLClassExpression ce = cardinality.getFiller();
        OWLObjectMaxCardinality weaker = dataFactory.getOWLObjectMaxCardinality(card + 1, prop, ce);
        OWLAxiom weakerAxiom = dataFactory.getOWLSubClassOfAxiom(ax.getSubClass(), weaker);
        return isEntailedWithReplacement(ax, weakerAxiom, axioms, checker);
    }

    private boolean isEntailedWithReplacement(OWLAxiom axiom, OWLAxiom replacementAxiom, Set<OWLAxiom> axioms, EntailmentChecker<OWLAxiom> checker) {
        axioms.remove(axiom);
        axioms.add(replacementAxiom);
        boolean entailed = checker.isEntailed(axioms);
        axioms.remove(replacementAxiom);
        axioms.add(axiom);
        return entailed;
    }


    private Set<OWLAxiom> getFlattenedAxioms(Set<OWLAxiom> axioms) {
        deltaTransformation = new DeltaTransformation(dataFactory);
        return deltaTransformation.transform(axioms);
    }


}
