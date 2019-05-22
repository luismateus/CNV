#!/bin/bash

PATH=/home/ec2-user/CNV-proj
#PATH=/home/hypz/Documents/workspace/CNV/CNV-proj


_JAVA_OPTIONS="-XX:-UseSplitVerifier"

cd $PATH
/bin/rm -f org/apache/commons/cli/*.class
/usr/bin/javac org/apache/commons/cli/*.java
/bin/rm -f *.class
/usr/bin/javac LaunchTask.java
/usr/bin/javac CleanInstances.java
/usr/bin/javac ScallingTask.java
/usr/bin/javac TerminateTask.java
/usr/bin/javac EC2.java
/usr/bin/javac LoadBalancer.java
/usr/bin/javac AutoScaler.java
/usr/bin/javac Manager.java

/usr/bin/java Manager