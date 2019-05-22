#!/bin/bash
#PATH=/home/hypz/Documents/workspace/CNV/CNV-proj
PATH=/home/ec2-user/CNV-proj



cd $PATH
/bin/rm *.class
/usr/bin/javac pt/ulisboa/tecnico/cnv/server/InstrumentationMetrics.java
#java pt/ulisboa/tecnico/cnv/server/InstrumentationMetrics pt/ulisboa/tecnico/cnv/solver/*.class
/usr/bin/javac pt/ulisboa/tecnico/cnv/server/WebServer.java
/usr/bin/java pt/ulisboa/tecnico/cnv/server/WebServer