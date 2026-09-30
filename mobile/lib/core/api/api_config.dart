abstract final class ApiConfig {
  /// Set per machine at build time:
  /// `flutter run --dart-define=API_BASE_URL=http://192.168.1.23:8080` (a phone on the same Wi-Fi).
  /// The default is how the Android emulator reaches the host Mac.
  static const baseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8080',
  );
}
