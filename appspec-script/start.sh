#!/bin/bash

sudo apt update
sudo apt install openjdk-17-jdk -y

sudo apt install cowsay lolcat -y

BASHRC=/home/ubuntu/.bashrc
COWSAY_CMD='cowsay -f $(ls /usr/share/cowsay/cows | shuf -n 1 | sed '\''s/\.cow$//'\'' ) "Welcome to Porthos Server!" | lolcat'

if ! grep -Fxq "$COWSAY_CMD" "$BASHRC"; then
  echo -e "\n# Porthos welcome banner" >> "$BASHRC"
  echo "$COWSAY_CMD" >> "$BASHRC"
fi

cd /home/ubuntu/porothos/build/libs
nohup java -jar noticecore-0.0.1-SNAPSHOT.jar > /home/ubuntu/app.log 2>&1 &

