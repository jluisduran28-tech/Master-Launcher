#!/bin/bash
./gradlew -q assembleDebug 2>&1 | tail -n 15
