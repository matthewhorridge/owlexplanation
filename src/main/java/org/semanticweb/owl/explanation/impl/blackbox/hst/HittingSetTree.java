package org.semanticweb.owl.explanation.impl.blackbox.hst;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.ExplanationGeneratorInterruptedException;
import org.semanticweb.owl.explanation.api.ExplanationProgressMonitor;
import org.semanticweb.owl.explanation.telemetry.*;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.reasoner.TimeOutException;

import java.io.OutputStream;
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
public class HittingSetTree<E> implements TelemetryObject {

    private HittingSetTreeNode<E> root;

    private Set<Set<OWLAxiom>> closedPaths = new HashSet<Set<OWLAxiom>>();

    private Set<OWLAxiom> justificationsUnion = new HashSet<OWLAxiom>();

    private List<Explanation<E>> explanations = new ArrayList<Explanation<E>>();

    private ExplanationProgressMonitor<E> progressMonitor;

    private Set<Set<OWLAxiom>> exploredPaths = new HashSet<Set<OWLAxiom>>();

    private ExplanationComparator<E> explanationComparator = new ExplanationComparator(this);

    private int treeSize = 0;

    private HashSet<Explanation<E>> allFoundExplanations = new HashSet<Explanation<E>>();

    private HittingSetTreeConstructionStrategy<E> strategy;

    private int numberOfNodesWithCallsToFindOne = 0;

    private int numberOfNodesWithReusedJustifications = 0;

    private int numberOfEarlyTerminatedPaths = 0;

    private int numberOfEarlyTerminatedClosedPaths = 0;

    private int closedPathMaxLength = 0;

    private int closedPathMinLength = Integer.MAX_VALUE;


    private int exploredPathMaxLength = 0;

    private int summedPathSize;


    private TelemetryInfo info;


    public HittingSetTree(HittingSetTreeConstructionStrategy<E> strategy, ExplanationProgressMonitor<E> progressMonitor) {
        this.progressMonitor = progressMonitor;
        this.strategy = strategy;
    }

    /**
     * The number of calls to find one.  Find one is called when there isn't a justification that can be reused.
     */
    public void incrementNumberOfNodesWithCallsToFindOne() {
        numberOfNodesWithCallsToFindOne++;
    }

    public void incrementNumberOfNodesWithReusedJustifications() {
        numberOfNodesWithReusedJustifications++;
    }


    public void buildHittingSetTree(E entailment, int limit, ExplanationGeneratorMediator<E> generatorMediator) {

        //////////////////////////////////////////////////////////
        TelemetryTimer hstTimer = new TelemetryTimer();
        info = new DefaultTelemetryInfo("hittingsettree", hstTimer);
        final TelemetryTransmitter transmitter = TelemetryTransmitter.getTransmitter();
        transmitter.beginTransmission(info);
        boolean foundAll = false;
        try {
            transmitter.recordMeasurement(info, "construction strategy", strategy.getClass().getName());
            hstTimer.start();
            //////////////////////////////////////////////////////////


            numberOfNodesWithCallsToFindOne = 1;
            Explanation<E> firstExplanation = generatorMediator.generateExplanation(entailment);
            root = new HittingSetTreeNode<E>(this, firstExplanation);
            treeSize = 1;
            addExplanation(firstExplanation);
            if (explanations.size() >= limit) {
                return;
            }
            strategy.constructTree(this, limit, generatorMediator);
            foundAll = true;
            //////////////////////////////////////////////////////////

        }
        catch (TimeOutException e) {
            transmitter.recordMeasurement(info, "reasoner time out", true);
            throw e;
        }
        catch (ExplanationGeneratorInterruptedException e) {
            transmitter.recordMeasurement(info, "hst interrupted", true);
            throw e;
        }
        catch (RuntimeException e) {
            transmitter.recordMeasurement(info, "hst exception", true);
            transmitter.recordMeasurement(info, "hst exception message", e.getMessage());
            transmitter.recordMeasurement(info, "hst exception class", e.getClass().getName());
            throw e;
        }
        finally {
            hstTimer.stop();
            transmitter.recordMeasurement(info, "number of nodes", treeSize);
            transmitter.recordMeasurement(info, "number of nodes with calls to findone", numberOfNodesWithCallsToFindOne);
            transmitter.recordMeasurement(info, "number of nodes with reused justifications", numberOfNodesWithReusedJustifications);
            transmitter.recordMeasurement(info, "number of closed paths", closedPaths.size());
            transmitter.recordMeasurement(info, "number of early terminated paths", numberOfEarlyTerminatedPaths);
            transmitter.recordMeasurement(info, "number of early terminated closed paths", numberOfEarlyTerminatedClosedPaths);
            transmitter.recordMeasurement(info, "closed path min length", closedPathMinLength);
            transmitter.recordMeasurement(info, "closed path max length", closedPathMaxLength);
            transmitter.recordMeasurement(info, "closed path average length", (summedPathSize * 1.0 / closedPaths.size()));
            transmitter.recordTiming(info, "construction time", hstTimer);
            transmitter.recordMeasurement(info, "found all", foundAll);
//            transmitter.recordObject(info, "hst", ".treeml", this);
            transmitter.endTransmission(info);
        }
        //////////////////////////////////////////////////////////

    }


    public Set<Set<OWLAxiom>> getExploredPaths() {
        return exploredPaths;
    }

    public ExplanationProgressMonitor<E> getProgressMonitor() {
        return progressMonitor;
    }

    public void addExplanation(Explanation<E> explanation) {
        if (!explanation.isEmpty()) {
            if (allFoundExplanations.add(explanation)) {
                explanations.add(explanation);
                Collections.sort(explanations, explanationComparator);
                progressMonitor.foundExplanation(null, explanation, allFoundExplanations);
            }
        }
    }

    public List<Explanation<E>> getSortedExplanations() {
        return explanations;
    }

    public Set<Explanation<E>> getExplanations() {
        return allFoundExplanations;
    }

    public HittingSetTreeNode<E> getRoot() {
        return root;
    }

    public boolean containsClosedPath(Set<OWLAxiom> path) {
        for(Set<OWLAxiom> closedPath : closedPaths) {
            if(closedPath.size() <= path.size()) {
                if(path.containsAll(closedPath)) {
                    numberOfEarlyTerminatedPaths++;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addExploredPath(Set<OWLAxiom> currentPath) {
        treeSize++;
        boolean added = exploredPaths.add(currentPath);
        if(added) {
            summedPathSize += currentPath.size();
        }
        else {
            numberOfEarlyTerminatedPaths++;
//            if (closedPaths.contains(currentPath)) {
//                numberOfEarlyTerminatedClosedPaths++;
//            }
        }
        if (currentPath.size() > exploredPathMaxLength) {
            exploredPathMaxLength = currentPath.size();
        }
        return added;
    }

    public void removeCurrentPath(Set<OWLAxiom> currentPath) {
        exploredPaths.remove(currentPath);
    }

    public void addClosedPath(Set<OWLAxiom> pathContents) {
        if (closedPaths.add(pathContents)) {
            if (pathContents.size() < closedPathMinLength) {
                closedPathMinLength = pathContents.size();
            }
            if (pathContents.size() > closedPathMaxLength) {
                closedPathMaxLength = pathContents.size();
            }
        }
    }

    public String getPreferredSerialisedName() {
        return "hst";
    }

    public boolean isSerialisedAsXML() {
        return false;
    }





























    public void serialise(OutputStream outputStream) {
//        this.writeTreeML(new PrintWriter(outputStream));
    }

//    public void serialise(TelemetryObjectXMLWriter writer) throws IOException {
//
//    }
//
//    private Map<OWLAxiom, Integer> axiom2IndexMap = new HashMap<OWLAxiom, Integer>();
//
//
//    public void writeGraphML(PrintWriter pw) {
//        writeGraphMLDocumentElementStart(pw);
//        writeGraphMLElementStart(pw);
//        writeGraphMLNodeAndChildren(root, pw);
//        writeGraphMLElementEnd(pw);
//        writeGraphMLDocumentElementEnd(pw);
//    }
//
//    private void writeGraphMLNodeAndChildren(HittingSetTreeNode<?> node, PrintWriter pw) {
//        writeGraphMLNodeElement(node, pw);
//        for (HittingSetTreeNode<?> child : node.getChildren()) {
//            // Add edge
//            writeGraphMLEdgeElement(node, child, pw);
//            writeGraphMLNodeAndChildren(child, pw);
//        }
//    }
//
//    private void writeGraphMLDocumentElementStart(PrintWriter pw) {
//        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//        pw.println("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"");
//        pw.println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
//        pw.println("xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns");
//        pw.println("http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">");
//    }
//
//    private void writeGraphMLDocumentElementEnd(PrintWriter pw) {
//        pw.println("</graphml>");
//    }
//
//    private void writeGraphMLElementStart(PrintWriter pw) {
//        pw.println("<graph edgedefault=\"directed\" parse.nodes=\"" + treeSize + "\">");
//    }
//
//    private void writeGraphMLElementEnd(PrintWriter pw) {
//        pw.println("</graph>");
//    }
//
//    private void writeGraphMLNodeElement(HittingSetTreeNode<?> node, PrintWriter pw) {
//        pw.print("<node id=\"");
//        pw.print(getNodeID(node));
//        pw.print("\"");
//        pw.println("/>");
//    }
//
//    private void writeGraphMLEdgeElement(HittingSetTreeNode<?> source, HittingSetTreeNode<?> sink, PrintWriter pw) {
//        pw.print("<edge source=\"");
//        pw.print(getNodeID(source));
//        pw.print("\" target=\"");
//        pw.print(getNodeID(sink));
//        pw.print("\"");
//        pw.println("/>");
//    }
//
//    private String getNodeID(HittingSetTreeNode<?> node) {
//        return Integer.toString(System.identityHashCode(node));
//    }
//
//    private String getEdgeID(HittingSetTreeNode<?> source, HittingSetTreeNode<?> sink) {
//        return "e";
//    }
//
//    public void writeTreeML(PrintWriter pw) {
//
//        axiom2IndexMap.clear();
//        pw.println("<tree>");
//
//        pw.println("<declarations>");
//        pw.println("<attributeDecl name=\"name\" type=\"String\"/>");
//        pw.println("</declarations>");
//
//        writeTreeMLNodeAndChildren(getRoot(), pw);
//        pw.println("</tree>");
//
//        pw.flush();
//    }
//
//    private <E> void writeTreeMLNodeAndChildren(HittingSetTreeNode<E> node, PrintWriter pw) {
//
//        List<HittingSetTreeNode<E>> children = node.getChildren();
//        if (children.size() == 0) {
//            // Leaf
//            pw.println("<leaf>");
//            pw.println("<attribute name=\"name\" value=\" " + renderAxioms(node) + " \"/>");
//            pw.println("</leaf>");
//        }
//        else {
//            pw.println("<branch>");
//            pw.println("<attribute name=\"name\" value=\" " + renderAxioms(node) + " \"/>");
//            for (HittingSetTreeNode<E> child : children) {
//                writeTreeMLNodeAndChildren(child, pw);
//            }
//            pw.println("</branch>");
//        }
//    }
//
//    private String renderAxioms(HittingSetTreeNode<?> node) {
//
//        StringBuilder sb = new StringBuilder();
//        sb.append("[");
//        sb.append(getAxiomID(node.getParentEdgeLabel()));
//        sb.append("] ");
//        sb.append("{");
//        if (node.getExplanation() != null) {
//            for (Iterator<OWLAxiom> it = node.getExplanation().getAxioms().iterator(); it.hasNext();) {
//                Integer i = getAxiomID(it.next());
//                sb.append(i);
//                if (it.hasNext()) {
//                    sb.append(", ");
//                }
//
//            }
//        }
//        sb.append("}");
//        if (node.isReuse()) {
//            sb.append("+");
//        }
//        return sb.toString();
//    }
//
//    private Integer getAxiomID(Object ax) {
//        if (ax instanceof OWLAxiom) {
//            Integer i = axiom2IndexMap.get((OWLAxiom) ax);
//            if (i == null) {
//                i = axiom2IndexMap.size() + 1;
//                axiom2IndexMap.put((OWLAxiom) ax, i);
//            }
//            return i;
//        }
//        else {
//            return null;
//        }
//    }


}
