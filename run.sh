#!/bin/bash
PATH=/home/hypz/Documents/workspace/CNV/CNV-proj
#PATH=/Users/joaorobalo/Desktop/CNV/CNV-proj

cd $PATH
/bin/rm pt/ulisboa/tecnico/cnv/server/WebServer.class
/usr/bin/javac pt/ulisboa/tecnico/cnv/server/WebServer.java
/usr/bin/java pt/ulisboa/tecnico/cnv/server/WebServer