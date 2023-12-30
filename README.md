## SCXML Sdk

IntelliJ and Gradle Plugins for modeling and integration of Harel Statecharts via _State Chart XML (SCXML)_.

Harel Statecharts are an extended form of Finit-State Machines (FSM). Harel Statecharts are part of Unified Modeling Language (UML).

[SCXML](https://www.w3.org/TR/scxml/) is not only a XML Schema, it specifies also a generic state-machine-based execution environment with an implementation in pseudocode.

This project tries to support graphical editing and execution on different platforms. 
The SCXML pseudocode from W3C is implemented as re-usable Java-library.

### === CONSTRUCTION SITE ===


Plan:
+ Graphical SCXML Editor for IntelliJ
  + SCXML Schema to support manual editor (done).
  + Interactive Graphical representation.
  + Extension of SCXML-files to store graphical properties (re-use of existing schemas if possible).
  + Editors for Executable Content with syntax/context support (language injection or some special LanguageTextField?)
+ IntelliJ Language Support for Executable Content: 
  + ECMAScript (supported by Java and Rust Runtime)
  + Java (supported by Java Runtime).
  + Java Expression Language.
+ Java Runtime 
  + As Maven Artefact (should be extracted later to some other project)
  + Execution according to W2C Pseudocode
  + Support for ECMAScript via JSR 292 (Nashorn or GraalVM JavaScript)
  + Support for Java (Generator & Compiler, org.eclipse.jdt.core needed?)
  + Support for Offline-Generation if Java-Language is used.
+ Gradle Plugin
  + Documentation Generator task (SVG from enriched SCXML, Java Generation) to support CI/CD.
