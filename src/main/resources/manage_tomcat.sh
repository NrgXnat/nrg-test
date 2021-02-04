#!/bin/sh

if [ $# -ne 2 ]; then
  echo "Pass in 2 positional arguments for the command (stop, start, restart) and service name (tomcat, tomcat7)."
  exit
fi

echo "Attempting to $1 tomcat...";
sudo service $2 $1