#!/bin/bash
# Android TV / Google TV: usar este launcher como pantalla de inicio (por ADB). Ver COMPATIBILIDAD.md.
# Uso: ./scripts/set-launcher.sh status | on | off
PKG=com.example.tvlauncher
GTV=com.google.android.apps.tv.launcherx     # Google TV
ATV=com.google.android.tvlauncher            # Android TV clásico
has() { adb shell pm list packages | tr -d '\r' | grep -qx "package:$1"; }
case "$1" in
status)
  adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME | tail -n 2
  for p in $GTV $ATV $PKG; do has $p && echo "instalado: $p"; done ;;
on)
  has $PKG || { echo "Instala primero el APK (./scripts/install.sh). Sin launcher instalado la TV se queda en negro."; exit 1; }
  echo "Se desactivará el launcher de Google para el usuario 0 (reversible con: $0 off)."
  read -p "¿Continuar? (si/no) " r; [ "$r" = "si" ] || exit 1
  for p in $GTV $ATV; do has $p && adb shell pm disable-user --user 0 $p; done
  adb shell cmd package set-home-activity $PKG/.MainActivity
  echo "Listo. Pulsa Home en el control para probar." ;;
off)
  for p in $GTV $ATV; do adb shell cmd package install-existing $p >/dev/null 2>&1; adb shell pm enable $p; done
  echo "Launcher de Google restaurado." ;;
*) echo "uso: $0 status|on|off"; exit 1 ;;
esac
