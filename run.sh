#!/bin/bash
#PATH=/home/hypz/Documents/workspace/CNV/CNV-proj
PATH=/home/ec2-user/CNV-proj

export CLASSPATH=$CLASSPATH:/home/ec2-user/aws-java-sdk-1.11.557/lib/aws-java-sdk-1.11.557.jar:/home/ec2-user/aws-java-sdk-1.11.557/third-party/lib/*:.


cd $PATH
/bin/rm -f org/apache/commons/cli/*.class
/usr/bin/javac org/apache/commons/cli/*.java
/bin/rm -f pt/ulisboa/tecnico/cnv/server/*.class
/usr/bin/javac pt/ulisboa/tecnico/cnv/server/InstrumentationMetrics.java
#java pt/ulisboa/tecnico/cnv/server/InstrumentationMetrics pt/ulisboa/tecnico/cnv/solver/*.class
/usr/bin/javac pt/ulisboa/tecnico/cnv/server/WebServer.java
/usr/bin/java pt/ulisboa/tecnico/cnv/server/WebServer
