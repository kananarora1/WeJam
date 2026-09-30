import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Holds the app JWT in memory only (never on disk; a fresh one is exchanged on every cold start).
/// A plain holder rather than Riverpod state: the HTTP layer needs it synchronously, and nothing
/// should rebuild when it changes.
class AppTokenStore {
  String? accessToken;
}

final appTokenStoreProvider = Provider<AppTokenStore>((ref) => AppTokenStore());
