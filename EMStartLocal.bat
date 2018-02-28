%ECHO OFF
javac *.java

START "EVENT MANAGER REGISTRY" /MIN /NORMAL rmiregistry
START "EVENT MANAGER" /MIN /NORMAL java MessageManager
