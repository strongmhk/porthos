#!/bin/bash

echo "[INFO] Preparing host volumes..."
mkdir -p /var/porthos/mysql /var/porthos/redis

echo "[INFO] Setting file ownership to ubuntu:ubuntu..."
sudo chown -R ubuntu:ubuntu /home/ubuntu/porthos

echo "[INFO] Starting MySQL & Redis containers..."
cd /home/ubuntu/porthos && docker compose up -d

# 기다림 (MySQL, Redis가 뜰 때까지)
until nc -z localhost 3306; do
  echo "[INFO] Waiting for MySQL..."
  sleep 2
done

until nc -z localhost 6379; do
  echo "[INFO] Waiting for Redis..."
  sleep 2
done

echo "[INFO] Launching Spring Boot application..."
nohup java -jar /home/ubuntu/porthos/build/libs/noticore-0.0.1-SNAPSHOT.jar > /home/ubuntu/porthos/app.log 2>&1 &

echo "[INFO] Application started with PID $!"
exit 0
