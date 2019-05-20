#!/bin/bash
export CLASSPATH="$CLASSPATH:BIT:BIT/samples:./"
_JAVA_OPTIONS="-XX:-UseSplitVerifier"

javac pt/ulisboa/tecnico/cnv/server/InstrumentationMetrics.java
java pt/ulisboa/tecnico/cnv/server/InstrumentationMetrics pt/ulisboa/tecnico/cnv/solver/AStarStrategy.class