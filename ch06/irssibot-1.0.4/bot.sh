#!/bin/sh

# IrssiBot start / stop script for Unix platforms

# for release, comment this
JAR_DIR=/usr/local/java/tools

# for release, uncomment this
#JAR_DIR=jars 

case "$1" in
  start)
	echo -n "starting IrssiBot.. "
	java -classpath "$JAR_DIR/xerces.jar:$JAR_DIR/mysql.jar:." irssibot.core.Core > bot.log 2>&1 &
	echo "[done]"
	;;
  stop)
	echo -n "stopping IrssiBot.. "
	kill `ps axuwww | grep "irssibot\.core\.Core" | grep -v grep | awk '{print $2}'`
	echo "[done]"
	;;
  *)
	echo "usage: $0 {start|stop}"
	exit 1
esac
