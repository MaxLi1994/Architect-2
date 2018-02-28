%ECHO OFF
javac *.java

START "EVENT MANAGER REGISTRY" /MIN /NORMAL rmiregistry
START "EVENT MANAGER" /MIN /NORMAL java -Djava.rmi.server.hostname="%1" MessageManager %1
