package org.semanticweb.owl.explanation.impl.rootderived;

import org.semanticweb.owl.explanation.api.ExplanationException;
import org.semanticweb.owl.explanation.api.RootDerivedReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
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
 * 07-Sep-2008<br><br>
 */
public class StructuralRootDerivedReasoner implements RootDerivedReasoner {

    private OWLOntologyManager man;

    private OWLReasoner reasoner;

    private OWLReasonerFactory reasonerFactory;

    private OWLOntology mergedOntology;

    private Map<OWLClass, Set<OWLClass>> child2Parent;

    private Map<OWLClass, Set<OWLClass>> parent2Child;

    private Set<OWLClass> roots;

    private boolean dirty;


    public StructuralRootDerivedReasoner(OWLOntologyManager man, OWLReasoner reasoner, OWLReasonerFactory reasonerFactory) {
        this.man = man;
        this.reasonerFactory = reasonerFactory;
        this.reasoner = reasoner;
        this.child2Parent = new HashMap<OWLClass, Set<OWLClass>>();
        this.parent2Child = new HashMap<OWLClass, Set<OWLClass>>();
        roots = new HashSet<OWLClass>();

        try {
            getMergedOntology();
        }
        catch (ExplanationException e) {
            e.printStackTrace();
        }
        dirty = true;
    }


    public OWLOntology getMergedOntology() throws ExplanationException {
        try {
            if (mergedOntology == null) {
                mergedOntology = man.createOntology(IRI.create("owlapi:ontology:merge"), reasoner.getRootOntology().getImportsClosure(), true);
            }
            return mergedOntology;
        }
        catch (OWLOntologyCreationException e) {
            throw new ExplanationException(e);
        }
        catch (OWLOntologyChangeException e) {
            throw new ExplanationException(e);
        }
    }

    private Set<OWLClass> get(OWLClass c, Map<OWLClass, Set<OWLClass>> map) {
        Set<OWLClass> set = map.get(c);
        if (set == null) {
            set = new HashSet<OWLClass>();
            map.put(c, set);
        }
        return set;
    }

    public Set<OWLClass> getDependentChildClasses(OWLClass cls) {
        return get(cls, parent2Child);
    }


    public Set<OWLClass> getDependentDescendantClasses(OWLClass cls) {
        Set<OWLClass> result = new HashSet<OWLClass>();
        getDescendants(cls, result);
        return result;
    }

    private void getDescendants(OWLClass cls, Set<OWLClass> result) {
        if (result.contains(cls)) {
            return;
        }
        for (OWLClass child : getDependentChildClasses(cls)) {
            result.add(child);
            getDescendants(child, result);
        }
    }


    public Set<OWLClass> getRootUnsatisfiableClasses() throws ExplanationException {
        if (dirty) {
            computeRootDerivedClasses();
        }
        return Collections.unmodifiableSet(roots);
    }


    private void computeRootDerivedClasses() throws ExplanationException {
            computeCandidateRoots();
            roots.remove(man.getOWLDataFactory().getOWLNothing());
            for (OWLClass child : child2Parent.keySet()) {
                for (OWLClass par : get(child, child2Parent)) {
                    get(par, parent2Child).add(child);
                }
            }
            // Now find cycles
            HashSet<OWLClass> processed = new HashSet<OWLClass>();
            HashSet<Set<OWLClass>> result = new HashSet<Set<OWLClass>>();

            for (OWLClass cls : child2Parent.keySet()) {
                if (!processed.contains(cls)) {
                    tarjan(cls, 0, new Stack<OWLClass>(), new HashMap<OWLClass, Integer>(), new HashMap<OWLClass, Integer>(), result, processed, new HashSet<OWLClass>());
                }


//                Set<List<OWLClass>> paths = new HashSet<List<OWLClass>>();
//                getPaths(cls, new ArrayList<OWLClass>(), new HashSet<OWLClass>(), paths);
//                for (List<OWLClass> path : paths) {
//                    System.out.println(path);
//                    if (path.size() > 2 && path.get(0).equals(path.get(path.size() - 1))) {
//                        System.out.println(path);
//                        roots.add(cls);
//                    }
//                }
            }
//            if(!result.isEmpty()) {
//                    System.out.println("CYCLES:");
//                    System.out.println(result);
//                    roots.add(cls);
//                }
            for (Set<OWLClass> res : result) {
                roots.addAll(res);
            }
//            System.out.println(result );
    }

    private void getPaths(OWLClass cls, List<OWLClass> curPath, Set<OWLClass> curPathSet, Set<List<OWLClass>> paths) {
        if (curPathSet.contains(cls)) {
            curPath.add(cls);
            paths.add(new ArrayList<OWLClass>(curPath));
            return;
        }
        curPathSet.add(cls);
        curPath.add(cls);
        Set<OWLClass> parents = child2Parent.get(cls);
        if (parents.isEmpty()) {
            paths.add(new ArrayList<OWLClass>(curPath));
        }
        for (OWLClass dep : parents) {
            getPaths(dep, curPath, curPathSet, paths);
            if (!curPath.isEmpty()) {
                curPath.remove(curPath.size() - 1);
                curPathSet.remove(dep);
            }
        }
    }


    public void tarjan(OWLClass cls, int index, Stack<OWLClass> stack, Map<OWLClass, Integer> indexMap, Map<OWLClass, Integer> lowlinkMap, Set<Set<OWLClass>> result, Set<OWLClass> processed, Set<OWLClass> stackClass) {
        processed.add(cls);
        indexMap.put(cls, index);
        lowlinkMap.put(cls, index);
        index = index + 1;
        stack.push(cls);
        stackClass.add(cls);
        for (OWLClass par : child2Parent.get(cls)) {
            if (!indexMap.containsKey(par)) {
                tarjan(par, index, stack, indexMap, lowlinkMap, result, processed, stackClass);
                lowlinkMap.put(cls, Math.min(lowlinkMap.get(cls), lowlinkMap.get(par)));
            }
            else if (stackClass.contains(par)) {
                lowlinkMap.put(cls, Math.min(lowlinkMap.get(cls), indexMap.get(par)));
            }
        }
        if (lowlinkMap.get(cls).equals(indexMap.get(cls))) {
            Set<OWLClass> scc = new HashSet<OWLClass>();
            while (true) {
                OWLClass clsPrime = stack.pop();
                stackClass.remove(clsPrime);
                scc.add(clsPrime);
                if (clsPrime.equals(cls)) {
                    break;
                }
            }
            if (scc.size() > 1) {
                result.add(scc);
            }
        }
    }


    private void pruneRoots() throws ExplanationException {
        try {
            Set<OWLClass> rootUnsatClses = new HashSet<OWLClass>(roots);
            List<OWLOntologyChange> appliedChanges = new ArrayList<OWLOntologyChange>();

            Set<OWLClass> potentialRoots = new HashSet<OWLClass>();
            for (OWLDisjointClassesAxiom ax : new ArrayList<OWLDisjointClassesAxiom>(mergedOntology.getAxioms(AxiomType.DISJOINT_CLASSES))) {
                for (OWLClass cls : rootUnsatClses) {
                    if (ax.getSignature().contains(cls)) {
                        RemoveAxiom chg = new RemoveAxiom(mergedOntology, ax);
                        man.applyChange(chg);
                        appliedChanges.add(chg);
                        for (OWLEntity ent : ax.getSignature()) {
                            if (ent.isOWLClass()) {
                                potentialRoots.add(ent.asOWLClass());
                            }
                        }
                    }
                }
            }

            for (OWLClass c : rootUnsatClses) {
                man.addAxiom(mergedOntology, man.getOWLDataFactory().getOWLDeclarationAxiom(c));
            }


            OWLReasoner checkingReasoner = reasonerFactory.createReasoner(mergedOntology);
            for (OWLClass root : new ArrayList<OWLClass>(rootUnsatClses)) {
                if (!potentialRoots.contains(root) && checkingReasoner.isSatisfiable(root)) {
                    rootUnsatClses.remove(root);
                }
            }
        }
        catch (OWLOntologyChangeException e) {
            throw new ExplanationException(e);
        }
    }


    private void computeCandidateRoots() throws ExplanationException {
        Set<OWLClass> unsatisfiableClasses = reasoner.getUnsatisfiableClasses().getEntities();
        OWLOntology ont = getMergedOntology();
        SuperClassChecker checker = new SuperClassChecker();
        for (OWLClass cls : unsatisfiableClasses) {
            checker.reset();
            for (OWLClassExpression sup : cls.getSuperClasses(reasoner.getRootOntology().getImportsClosure())) {
                sup.accept(checker);
            }
            for (OWLClassExpression sup : cls.getEquivalentClasses(reasoner.getRootOntology().getImportsClosure())) {
                sup.accept(checker);
            }
            Set<OWLClass> dependencies = checker.getDependencies();
            child2Parent.put(cls, new HashSet<OWLClass>(dependencies));
            if (dependencies.isEmpty()) {
                // Definite root?
                roots.add(cls);
            }
        }
    }


    private class SuperClassChecker implements OWLClassExpressionVisitor {

        private Set<OWLClass> dependsOn;

        private int modalDepth;

        private Map<Integer, Set<OWLObjectAllValuesFrom>> modalDepth2UniversalRestrictionPropertyMap;

        private Map<Integer, Set<OWLObjectPropertyExpression>> modalDepth2ExistsRestrictionPropertyMap;


        public SuperClassChecker() {
            modalDepth2UniversalRestrictionPropertyMap = new HashMap<Integer, Set<OWLObjectAllValuesFrom>>();
            modalDepth2ExistsRestrictionPropertyMap = new HashMap<Integer, Set<OWLObjectPropertyExpression>>();
            dependsOn = new HashSet<OWLClass>();
            modalDepth = 0;
        }


        public void addUniversalRestrictionProperty(OWLObjectAllValuesFrom r) {
            Set<OWLObjectAllValuesFrom> props = modalDepth2UniversalRestrictionPropertyMap.get(modalDepth);
            if (props == null) {
                props = new HashSet<OWLObjectAllValuesFrom>();
                modalDepth2UniversalRestrictionPropertyMap.put(modalDepth, props);
            }
            props.add(r);
        }


        public void addExistsRestrictionProperty(OWLObjectPropertyExpression prop) {
            Set<OWLObjectPropertyExpression> props = modalDepth2ExistsRestrictionPropertyMap.get(modalDepth);
            if (props == null) {
                props = new HashSet<OWLObjectPropertyExpression>();
                modalDepth2ExistsRestrictionPropertyMap.put(modalDepth, props);
            }
            props.add(prop);
        }


        public Set<OWLClass> getDependencies() {
//            for (int depth : modalDepth2UniversalRestrictionPropertyMap.keySet()) {
//                Set<OWLObjectPropertyExpression> successors = modalDepth2ExistsRestrictionPropertyMap.get(depth);
//                if (successors == null) {
//                    continue;
//                }
//                for (OWLObjectAllValuesFrom r : modalDepth2UniversalRestrictionPropertyMap.get(depth)) {
//                    if (successors.contains(r.getProperty())) {
//                        if (!r.getFiller().isAnonymous()) {
//                            dependsOn.add(r.getFiller().asOWLClass());
//                        }
//                    }
//                }
//            }
            return Collections.unmodifiableSet(dependsOn);
        }


        public void reset() {
            dependsOn.clear();
            modalDepth2ExistsRestrictionPropertyMap.clear();
            modalDepth2UniversalRestrictionPropertyMap.clear();
        }


        public void visit(OWLClass desc) {
            if (!reasoner.isSatisfiable(desc)) {
                dependsOn.add(desc);
            }
        }


        public void visit(OWLDataAllValuesFrom desc) {

        }


        public void visit(OWLDataExactCardinality desc) {

        }


        public void visit(OWLDataMaxCardinality desc) {
        }


        public void visit(OWLDataMinCardinality desc) {
        }


        public void visit(OWLDataSomeValuesFrom desc) {
        }


        public void visit(OWLDataHasValue desc) {
        }


        public void visit(OWLObjectAllValuesFrom desc) {
            if (desc.getFiller().isAnonymous()) {
                modalDepth++;
                desc.getFiller().accept(this);
                modalDepth--;
            }
            else {
                if (!reasoner.isSatisfiable(desc.getFiller())) {
                    addUniversalRestrictionProperty(desc);
                    dependsOn.add(desc.getFiller().asOWLClass());
                }
            }
        }


        public void visit(OWLObjectComplementOf desc) {

        }


        public void visit(OWLObjectExactCardinality desc) {
            if (desc.getFiller().isAnonymous()) {
                modalDepth++;
                desc.getFiller().accept(this);
                modalDepth--;
            }
            else {
                if (!reasoner.isSatisfiable(desc.getFiller())) {
                    if (!desc.getFiller().isAnonymous()) {
                        dependsOn.add(desc.getFiller().asOWLClass());
                    }
                }
            }
            addExistsRestrictionProperty(desc.getProperty());
        }


        public void visit(OWLObjectIntersectionOf desc) {
            for (OWLClassExpression op : desc.getOperands()) {
                if (op.isAnonymous()) {
                    op.accept(this);
                }
                else {
                    if (!reasoner.isSatisfiable(op)) {
                        dependsOn.add(op.asOWLClass());
                    }
                }
            }
        }


        public void visit(OWLObjectMaxCardinality desc) {

        }


        public void visit(OWLObjectMinCardinality desc) {
            if (desc.getFiller().isAnonymous()) {
                modalDepth++;
                desc.getFiller().accept(this);
                modalDepth--;
            }
            else {
                if (!reasoner.isSatisfiable(desc.getFiller())) {
                    dependsOn.add(desc.getFiller().asOWLClass());
                }
            }
            addExistsRestrictionProperty(desc.getProperty());
        }


        public void visit(OWLObjectOneOf desc) {
        }


        public void visit(OWLObjectHasSelf desc) {
            addExistsRestrictionProperty(desc.getProperty());
        }


        public void visit(OWLObjectSomeValuesFrom desc) {
            if (desc.getFiller().isAnonymous()) {
                modalDepth++;
                desc.getFiller().accept(this);
                modalDepth--;
            }
            else {
                if (!reasoner.isSatisfiable(desc.getFiller())) {
                    dependsOn.add(desc.getFiller().asOWLClass());
                }
            }
            addExistsRestrictionProperty(desc.getProperty());
        }


        public void visit(OWLObjectUnionOf desc) {
            for (OWLClassExpression op : desc.getOperands()) {
                if (reasoner.isSatisfiable(op)) {
                    return;
                }
            }
            for (OWLClassExpression op : desc.getOperands()) {
                if (op.isAnonymous()) {
                    op.accept(this);
                }
                else {
                    dependsOn.add(op.asOWLClass());
                }
            }
        }


        public void visit(OWLObjectHasValue desc) {
            addExistsRestrictionProperty(desc.getProperty());
        }
    }
}
