#!/bin/bash
# Compila, instala y abre el launcher
set -e
./scripts/build.sh && ./scripts/install.sh
adb shell am start -n com.example.tvlauncher/.MainActivity
