#!/bin/bash
cd /home/ubuntu/porothos/build/libs
nohup java -jar noticecore-0.0.1-SNAPSHOT.jar > /home/ubuntu/app.log 2>&1 &
