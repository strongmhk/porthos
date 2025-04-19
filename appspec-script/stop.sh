#!/bin/bash
PID=$(pgrep -f 'noticecore-0.0.1-SNAPSHOT.jar')
if [ -n "$PID" ]; then
  kill -9 $PID
fi

