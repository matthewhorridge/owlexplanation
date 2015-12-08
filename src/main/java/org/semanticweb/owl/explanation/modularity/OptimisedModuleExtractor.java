package org.semanticweb.owl.explanation.modularity;

//import com.clarkparsia.owlapi.modularity.locality.LocalityClass;
//import com.clarkparsia.owlapi.modularity.locality.SyntacticLocalityEvaluator;
//import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
//import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
//import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

import java.io.File;
import java.util.*;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 19/06/2012
 */
public class OptimisedModuleExtractor {
//
//
//
////    private Map<OWLEntity, Collection<OWLAxiom>> sigMap = new HashMap<OWLEntity, Collection<OWLAxiom>>();
//
//    private OWLOntology ontology;
//
//    Set<OWLAxiom> workingAxioms;
//
//    private int initialSize;
//
//    public OptimisedModuleExtractor(OWLOntology ontology) {
//        this.ontology = ontology;
//        this.initialSize = ontology.getLogicalAxiomCount();
//    }
//
//    public Set<OWLAxiom> getModule(Set<OWLEntity> sig) {
//        // Nested TOP BOTTOM *
//
////        workingAxioms = new HashSet<OWLAxiom>(initialSize);
////        for(AxiomType<?> axiomType : AxiomType.AXIOM_TYPES) {
////            if(axiomType.isLogical()) {
////                workingAxioms.addAll(ontology.getAxioms(axiomType));
////            }
////        }
////
////        LocalityClass localityClass = LocalityClass.BOTTOM_BOTTOM;
////        int size = 0;
////        int count = 0;
////        while (true) {
////            SyntacticLocalityEvaluator sle = new SyntacticLocalityEvaluator(localityClass);
//////            long tM0 = System.currentTimeMillis();
////            Set<OWLAxiom> module = extractModule(sig, workingAxioms, sle);
//////            long tM1 = System.currentTimeMillis();
////            if(module.size() == size) {
//////                long t0 = System.currentTimeMillis();
//////                SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(OWLManager.createOWLOntologyManager(), null, ontology.getAxioms(), ModuleType.STAR);
//////                Set<OWLAxiom> properModule = extractor.extract(sig);
//////                long t1 = System.currentTimeMillis();
//////                System.out.println(properModule.equals(module));
//////                long tMD = tM1 - tM0;
//////                System.out.println("Extracted module: (in " + tMD + ") " + module.size());
//////                long tD = t1 - t0;
//////                System.out.println("Proper module   : (in " + tD + ") " + properModule.size());
//////                System.out.println(tD / (tMD * 1.0));
//////                System.out.println();
//////                System.out.println();
//////                System.out.println();
////                return module;
////            }
////            else {
////                count++;
////            }
////            size = module.size();
////            if(localityClass == LocalityClass.TOP_TOP) {
////                localityClass = LocalityClass.BOTTOM_BOTTOM;
////            }
////            else {
////                localityClass = LocalityClass.TOP_TOP;
////            }
////            workingAxioms = module;
////        }
//    }
//
//    private Set<OWLAxiom> extractModule(Set<OWLEntity> sig, Set<OWLAxiom> workingAxioms, SyntacticLocalityEvaluator sle) {
//
//
//        Set<OWLAxiom> globals = new HashSet<OWLAxiom>();
//        Set<OWLAxiom> module = new HashSet<OWLAxiom>();
//
//        Set<OWLEntity> workingSignature = new HashSet<OWLEntity>(sig);
//        // Part 1 - Global axioms - axioms that appear in the module regardless of signature (?!?)
//        for(OWLAxiom alpha : workingAxioms) {
//            if(!sle.isLocal(alpha, Collections.<OWLEntity>emptySet())) {
//                globals.add(alpha);
//            }
//        }
//
//        for(OWLAxiom axiom : globals) {
//            addNonLocal(axiom, sig, module, workingSignature, new LinkedList<OWLEntity>(), sle);
//        }
//
//
//        Queue<OWLEntity> queue = new LinkedList<OWLEntity>(workingSignature);
//        while (!queue.isEmpty()) {
//            OWLEntity entity = queue.poll();
//            workingSignature.remove(entity);
//            for(OWLAxiom alpha : ontology.getReferencingAxioms(entity)) {
//                if(workingAxioms.contains(alpha) && !sle.isLocal(alpha, sig)) {
//                    boolean  b = addNonLocal(alpha, sig, module, workingSignature, queue, sle);
//                }
//            }
//        }
//
//
//        return module;
//    }
//
//    private boolean addNonLocal(OWLAxiom alpha, Set<OWLEntity> sig, Set<OWLAxiom> module, Set<OWLEntity> workingSignature, Queue<OWLEntity> workingQueue, SyntacticLocalityEvaluator sle) {
//        if(module.add(alpha)) {
//            Set<OWLEntity> alphaSig = alpha.getSignature();
//            for(OWLEntity entity : alphaSig) {
//                if(!sig.contains(entity)) {
//                    if(workingSignature.add(entity)) {
//                        workingQueue.add(entity);
//                    }
//                }
//            }
//            sig.addAll(alphaSig);
//            return true;
//        }
//        return false;
//    }
//
//
////    public static void main(String[] args) {
////        try {
////            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
////            OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("/tmp/profilingtest/ontologies/mccl/mccl.owl.xml"));
////            for(OWLAxiom ax : ont.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION)) {
////                manager.removeAxiom(ont, ax);
////            } for(OWLAxiom ax : ont.getAxioms(AxiomType.DATA_PROPERTY_ASSERTION)) {
////                manager.removeAxiom(ont, ax);
////            }
////            OWLOntology entailmentOnt = manager.loadOntologyFromOntologyDocument(new File("/tmp/profilingtest/telemetry-out/mccl/hermit-entailments/entailments.xml"));
////            Set<OWLAxiom> logicalAxioms = new HashSet<OWLAxiom>(ont.getAxioms());
////            OptimisedModuleExtractor extractor = new OptimisedModuleExtractor(ont);
////
////            for(OWLAxiom entailment : entailmentOnt.getLogicalAxioms()) {
////                logicalAxioms.remove(entailment);
////                Set<OWLAxiom> module = extractor.getModule(entailment.getSignature());
////                System.out.println("Module size: " + module.size());
//////                if(module.size() < 5) {
//////                    System.out.println(entailment);
//////                    System.out.println(":");
//////                    for(OWLAxiom ax : module) {
//////                        System.out.println(ax);
//////                    }
//////                }
////                logicalAxioms.add(entailment);
////            }
////        }
////        catch (OWLOntologyCreationException e) {
////            e.printStackTrace();
////        }
////    }
}
