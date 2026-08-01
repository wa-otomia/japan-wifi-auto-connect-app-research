#!/bin/sh
set -eu
cd "$(dirname "$0")"
swiftc -O -framework CoreWLAN -framework Foundation JWiFiCore.swift -o jwifi-core
codesign --force --sign - jwifi-core
echo "Built $(pwd)/jwifi-core"
