#!/bin/bash
# Solo errores del launcher, ultimas 30 lineas
adb logcat -d -s AndroidRuntime:E | tail -n 30
