import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_error.dart';
import '../../../core/api/app_token_store.dart';
import '../../auth/data/phone_auth_service.dart';
import '../../auth/domain/auth_models.dart';
import '../../auth/presentation/auth_providers.dart';
import '../data/account_repository.dart';
import '../domain/account.dart';

/// The backend session for the signed-in Firebase user: exchanges the Firebase ID token for an
/// app JWT, then loads `/me`. Null when signed out. Rebuilds whenever the Firebase user changes.
final sessionControllerProvider =
    AsyncNotifierProvider<SessionController, Account?>(
      SessionController.new,
      // Failures are shown with a "Try again" button; don't retry silently behind it.
      retry: (_, _) => null,
    );

class SessionController extends AsyncNotifier<Account?> {
  Future<bool>? _reauthInFlight;

  @override
  Future<Account?> build() async {
    final uid = await ref.watch(
      authStateProvider.selectAsync((user) => user?.uid),
    );
    final tokens = ref.read(appTokenStoreProvider);
    if (uid == null) {
      tokens.accessToken = null;
      return null;
    }
    await _exchange(forceRefresh: false);
    return ref.read(accountRepositoryProvider).me();
  }

  /// Called by the HTTP layer after a 401. Concurrent 401s share one attempt.
  /// Returns true when a fresh app JWT is in place.
  Future<bool> reauthenticate() => _reauthInFlight ??= _reauthenticate()
      .whenComplete(() => _reauthInFlight = null);

  /// A write endpoint returned the updated profile; use it instead of refetching `/me`.
  void accountUpdated(Account account) => state = AsyncData(account);

  /// For the error screen's "Try again".
  Future<void> retry() async {
    ref.invalidateSelf();
    await future.catchError((_) => null);
  }

  Future<bool> _reauthenticate() async {
    try {
      await _exchange(forceRefresh: true);
      return true;
    } on ApiFailure catch (e) {
      // The backend rejected a freshly refreshed Firebase token: this session is over.
      if (e.isUnauthorized) await ref.read(phoneAuthServiceProvider).signOut();
      return false;
    } on AuthFailure {
      return false;
    }
  }

  Future<void> _exchange({required bool forceRefresh}) async {
    final idToken = await ref
        .read(phoneAuthServiceProvider)
        .getIdToken(forceRefresh: forceRefresh);
    final accessToken = await ref
        .read(accountRepositoryProvider)
        .exchange(idToken);
    ref.read(appTokenStoreProvider).accessToken = accessToken;
  }
}
