package org.semanticweb.owl.explanation.api;

import org.semanticweb.owl.explanation.telemetry.TelemetryObject;
import org.semanticweb.owlapi.model.*;

import java.io.*;
import java.util.Collections;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 26/01/2011
 */
public class ExplanationTelemetryWrapper implements TelemetryObject {

    private Explanation<OWLAxiom> explanation;

    public ExplanationTelemetryWrapper(Explanation<OWLAxiom> explanation) {
        this.explanation = explanation;
    }

    public String getPreferredSerialisedName() {
        return "justification.owl.xml";
    }

    public boolean isSerialisedAsXML() {
        return true;
    }

    public void serialise(OutputStream outputStream) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Explanation.store(explanation, bos);
        String rendering = new String(bos.toByteArray());
        rendering = rendering.replace("<?xml version=\"1.0\"?>\n", "");
        PrintWriter pw = new PrintWriter(outputStream);
        pw.println(rendering);
        pw.flush();
    }


}
