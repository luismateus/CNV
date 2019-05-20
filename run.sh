#!/bin/bash
PATH=/home/ec2-user/CNV-proj

cd $PATH
/bin/rm pt/ulisboa/tecnico/cnv/server/WebServer.class
/bin/rm pt/ulisboa/tecnico/cnv/server/InstrumentationMetrics.class
/usr/bin/javac pt/ulisboa/tecnico/cnv/server/InstrumentationMetrics.class
#java pt/ulisboa/tecnico/cnv/server/InstrumentationMetrics pt/ulisboa/tecnico/cnv/solver/*.class
/usr/bin/javac pt/ulisboa/tecnico/cnv/server/WebServer.java
/usr/bin/java pt/ulisboa/tecnico/cnv/server/WebServer