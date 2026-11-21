#!/bin/bash
# Conecta la TV por ADB: ./scripts/connect.sh 192.168.1.50
adb connect "${1:?uso: connect.sh IP}:5555"
