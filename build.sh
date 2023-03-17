#!/usr/bin/env bash

cd $(dirname "${BASH_SOURCE[0]}")
./gradlew jar
cp game/build/libs/wolfdungeon3d.jar .
