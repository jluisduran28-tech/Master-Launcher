#!/bin/bash
# Sube versionCode en 1
f=app/build.gradle.kts
n=$(grep -oP 'versionCode = \K[0-9]+' $f)
sed -i "s/versionCode = $n/versionCode = $((n+1))/" $f
echo "versionCode -> $((n+1))"
