# OWL Explanation

[![Build](https://github.com/matthewhorridge/owlexplanation/actions/workflows/build.yml/badge.svg?branch=version5)](https://github.com/matthewhorridge/owlexplanation/actions/workflows/build.yml)

An API and reference implementation for generating justifications for entailments in OWL ontologies.

## Maven dependency

```xml
<dependency>
    <groupId>net.sourceforge.owlapi</groupId>
    <artifactId>owlexplanation</artifactId>
    <version>5.0.1</version>
</dependency>
```

## Example usage

```java
import org.semanticweb.owl.explanation.api.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

OWLReasonerFactory rf = ; // Get hold of a reasoner factory
OWLOntology ont = ; // Reference to an OWLOntology

// Create the explanation generator factory which uses reasoners provided by the specified
// reasoner factory
ExplanationGeneratorFactory<OWLAxiom> genFac = ExplanationManager
        .createExplanationGeneratorFactory(rf, OWLManager::createOWLOntologyManager);

// Now create the actual explanation generator for our ontology
ExplanationGenerator<OWLAxiom> gen = genFac.createExplanationGenerator(ont);

// Ask for explanations for some entailment
OWLAxiom entailment ; // Get a reference to the axiom that represents the entailment that we want explanation for

// Get our explanations.  Ask for a maximum of 5.
Set<Explanation<OWLAxiom>> expl = gen.getExplanations(entailment, 5);
```

## Explanations for inconsistent ontologies

To obtain explanations for inconsistent ontologies, use
`InconsistentOntologyExplanationGeneratorFactory`. Ask for explanations for
`SubClassOf(owl:Thing owl:Nothing)`.

## Building

Build the library and run its tests with:

```bash
mvn --batch-mode clean verify
```

Release instructions are documented in [RELEASING.md](RELEASING.md).
