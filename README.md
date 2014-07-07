OWL Explanation
==============

An API and reference implementation for generating justifications for entailments in OWL ontologies.

Maven Dependency
----------------
```xml
<dependency>
    <groupId>net.sourceforge.owlapitools</groupId>
    <artifactId>owlexplanation</artifactId>
    <version>1.0.0</version>
</dependency>
```

Example Usage
-------------
```java
import org.semanticweb.owl.explanation.api.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

OWLReasonerFactory rf = ; // Get hold of a reasoner factory
OWLOntology ont = ; // Reference to an OWLOntology

// Create the explanation generator factory which uses reasoners provided by the specified
// reasoner factory
ExplanationGeneratorFactory<OWLAxiom> genFac = ExplanationManager.createExplanationGeneratorFactory(rf);

// Now create the actual explanation generator for our ontology
ExplanationGenerator<OWLAxiom> gen = genFac.createExplanationGenerator(ont);

// Ask for explanations for some entailment
OWLAxiom entailment ; // Get a reference to the axiom that represents the entailment that we want explanation for

// Get our explanations.  Ask for a maximum of 5.
Set<Explanation<OWLAxiom>> expl = gen.getExplanations(entailment, 5);
```

Explanations for Inconsistent Ontologies
----------------------------------------

To obtain explanations for inconsistent ontologies the ```InconsistentOntologyExplanationGeneratorFactory``` can
be used.  Ask for explanations for ```SubClassOf(owl:Thing owl:Nothing)```.

