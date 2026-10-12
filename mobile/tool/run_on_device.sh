#!/bin/zsh
# Runs the app on the connected phone or emulator against the backend on this Mac.
# adb reverse makes the device's localhost:8080 (API) and localhost:9000 (photos, uploads) reach the
# Mac over the debug connection, so the Mac's Wi-Fi IP never matters. Extra arguments go to
# `flutter run`, e.g. `--profile`. With several devices attached, pick one with ANDROID_SERIAL.
set -e
devices=(${(f)"$(adb devices | awk 'NR > 1 && $2 == "device" { print $1 }')"})
serial=${ANDROID_SERIAL:-}
if [[ -z $serial ]]; then
  if (( ${#devices} == 1 )); then
    serial=${devices[1]}
  elif (( ${#devices} == 0 )); then
    echo "No device connected. Turn on wireless debugging (or plug in USB), then check: adb devices"
    exit 1
  else
    echo "More than one device is connected:"
    printf '  %s\n' $devices
    echo "Pick one, e.g.:  ANDROID_SERIAL=${devices[1]} zsh $0 $*"
    echo "(The same phone listed twice? Remove one with: adb disconnect ${devices[1]})"
    exit 1
  fi
fi
adb -s $serial reverse tcp:8080 tcp:${BACKEND_PORT:-8080}
adb -s $serial reverse tcp:9000 tcp:9000
cd "$(dirname "$0")/.."
flutter run -d $serial --dart-define=API_BASE_URL=http://localhost:8080 "$@"
