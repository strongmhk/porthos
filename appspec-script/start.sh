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

# Spring Boot 헬스체크 기반 재시도
MAX_RETRIES=5
RETRY_DELAY=10
COUNT=1

while [ $COUNT -le $MAX_RETRIES ]; do
  echo "[INFO] Attempt $COUNT: Launching Spring Boot..."
  sleep 10
  nohup java -jar /home/ubuntu/porthos/build/libs/noticore-0.0.1-SNAPSHOT.jar > /home/ubuntu/porthos/app.log 2>&1 &

  echo "[INFO] Checking /api/test health..."
  if curl -fs http://localhost:8080/api/test > /dev/null; then
    echo "[INFO] Spring Boot is healthy!"
    exit 0
  fi

  echo "[WARN] Health check failed. Retrying in ${RETRY_DELAY}s..."
  sleep $RETRY_DELAY
  COUNT=$((COUNT+1))
done

echo "[ERROR] Spring Boot failed to start after $MAX_RETRIES attempts."
exit 1
