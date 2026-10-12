abstract final class ApiConfig {
  /// Set at build time. On a phone or emulator use `tool/run_on_device.sh`, which sets up
  /// `adb reverse` and passes `http://localhost:8080`, so the Mac's Wi-Fi IP never matters.
  /// The default is how a plain Android emulator reaches the host Mac.
  static const baseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8080',
  );
}
