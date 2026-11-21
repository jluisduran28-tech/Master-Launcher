#!/bin/bash
# Samsung (Tizen). Requiere el CLI "tizen" (Tizen Studio o extensión VS Code) y un perfil de certificado.
# Uso: ./scripts/package-tizen.sh MI_PERFIL_DE_CERTIFICADO [IP_TV]
set -e
CERT="${1:?uso: package-tizen.sh PERFIL_CERT [IP_TV]}"
rm -rf dist/tizen && mkdir -p dist/tizen
cp web/index.html tizen/config.xml tizen/icon.png dist/tizen/
tizen package -t wgt -s "$CERT" -- dist/tizen
echo "Paquete generado en dist/tizen (*.wgt)"
if [ -n "$2" ]; then sdb connect "$2" && tizen install -n dist/tizen/TVLauncher.wgt -t "$2"; fi
