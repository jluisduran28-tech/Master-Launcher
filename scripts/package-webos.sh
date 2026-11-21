#!/bin/bash
# LG (webOS). Requiere el CLI ares-* (webOS TV SDK) y la TV en modo desarrollador.
# Uso: ./scripts/package-webos.sh [NOMBRE_DISPOSITIVO_ARES]
set -e
rm -rf dist/webos && mkdir -p dist/webos
cp web/index.html webos/appinfo.json webos/icon.png webos/largeIcon.png dist/webos/
ares-package dist/webos -o dist
echo "Paquete generado en dist/ (*.ipk)"
if [ -n "$1" ]; then ares-install -d "$1" dist/com.example.tvlauncher_4.0.0_all.ipk && ares-launch -d "$1" com.example.tvlauncher; fi
