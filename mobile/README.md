# WeJam — mobile

Flutter app (Android for now). Open this folder in VS Code.

## One-time setup

Firebase client config is **not committed** (it contains the project's API key). Generate it locally:

```bash
# Firebase CLI + FlutterFire CLI, logged in to the account that owns wejam-dev
firebase login
dart pub global activate flutterfire_cli

cd mobile
flutterfire configure --project=wejam-dev --platforms=android \
  --android-package-name=com.kananarora.wejam --out=lib/firebase_options.dart
```

This writes `lib/firebase_options.dart` and `android/app/google-services.json` (both gitignored).

## Run

```bash
cd mobile
flutter devices
flutter run -d <device-id>            # debug: hot reload, slower animations
flutter run --profile -d <device-id>  # judge animations / performance
flutter test && flutter analyze
```

Phone login during development uses the Firebase test number `+91 1234567890`, code `123456`.
