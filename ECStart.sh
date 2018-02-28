#!/bin/bash
# Environmental Control System start-up script
echo -n -e "\033]0;ECS CONSOLE\007"
echo "Starting TemperatureController"
java TemperatureController $1&
echo "Starting HumidityController"
java HumidityController $1&
echo "Starting TemperatureSensor"
java TemperatureSensor $1&
echo "Starting HumiditySensor"
java HumiditySensor $1&
echo "Starting ECSConsole"
java ECSConsole $1