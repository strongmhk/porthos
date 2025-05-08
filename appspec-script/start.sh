#!/bin/bash

echo "[INFO] Preparing host volumes..."
mkdir -p /var/porthos/mysql /var/porthos/redis

echo "[INFO] Setting file ownership to ubuntu:ubuntu..."
sudo chown -R ubuntu:ubuntu /home/ubuntu/porthos


echo "[INFO] Launching Spring Boot..."
nohup java -jar /home/ubuntu/porthos/build/libs/noticore-0.0.1-SNAPSHOT.jar > /home/ubuntu/porthos/app.log 2>&1 &
exit 0
