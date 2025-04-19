#!/bin/bash

# noticecore JAR 종료
JAR_NAME="noticecore-0.0.1-SNAPSHOT.jar"
PID=$(pgrep -f $JAR_NAME)

if [ -n "$PID" ]; then
  kill -9 $PID
fi

