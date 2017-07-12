#!/bin/sh

if [ $# -ne 1 ]; then
  echo "Pass in a single unflagged argument (stop, start, restart)."
  exit
fi

echo "Attempting to $1 tomcat...";
sudo /sbin/service tomcat6 $1