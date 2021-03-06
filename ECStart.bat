%ECHO OFF
%ECHO Starting ECS System
PAUSE
javac *.java

%ECHO Starting Temperature Controller Console
START "TEMPERATURE CONTROLLER CONSOLE" /MIN /NORMAL java TemperatureController %1 %2
START "TEMPERATURE CONTROLLER CONSOLE" /MIN /NORMAL java TemperatureController %1 %2
%ECHO Starting Humidity Sensor Console
START "HUMIDITY CONTROLLER CONSOLE" /MIN /NORMAL java HumidityController %1 %2
START "HUMIDITY CONTROLLER CONSOLE" /MIN /NORMAL java HumidityController %1 %2
START "TEMPERATURE SENSOR CONSOLE" /MIN /NORMAL java TemperatureSensor %1 %2
START "TEMPERATURE SENSOR CONSOLE" /MIN /NORMAL java TemperatureSensor %1 %2
%ECHO Starting Humidity Sensor Console
START "HUMIDITY SENSOR CONSOLE" /MIN /NORMAL java HumiditySensor %1 %2
%ECHO ECS Monitoring Console
START "MUSEUM ENVIRONMENTAL CONTROL SYSTEM CONSOLE" /NORMAL java ECSConsole %1 %2