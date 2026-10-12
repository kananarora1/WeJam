import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_error.dart';
import '../data/account_repository.dart';
import '../domain/account.dart';
import 'session_controller.dart';

/// State of the "Who's coming tonight?" form (design 1.4). Auto-disposed, so it starts fresh each visit.
final nameControllerProvider =
    NotifierProvider.autoDispose<NameController, NameState>(NameController.new);

/// "I'm here to…" on the onboarding screen. Changeable later from Profile.
enum HereFor {
  playOrListen(null),
  hostJams(Role.host),
  runAVenue(Role.venueAdmin);

  const HereFor(this.role);

  /// The role this adds on top of the default USER role, if any.
  final Role? role;
}

@immutable
class NameState {
  const NameState({this.submitting = false, this.error});

  final bool submitting;
  final String? error;
}

class NameController extends Notifier<NameState> {
  /// Same limit as the backend's `@Size(max = 50)` on `displayName`.
  static const maxLength = 50;

  @override
  NameState build() => const NameState();

  static bool canSubmit(String raw) => validate(raw) == null;

  static String? validate(String raw) {
    final name = raw.trim();
    if (name.isEmpty) return 'Enter the name you go by.';
    if (name.length > maxLength) {
      return 'Keep it to $maxLength characters or fewer.';
    }
    return null;
  }

  Future<void> submit(
    String raw, {
    HereFor hereFor = HereFor.playOrListen,
  }) async {
    if (state.submitting) return;
    final invalid = validate(raw);
    if (invalid != null) {
      state = NameState(error: invalid);
      return;
    }

    state = const NameState(submitting: true);
    try {
      final session = ref.read(sessionControllerProvider.notifier);
      // The role goes first: the router leaves this screen the moment a name exists, so the role
      // (and the token carrying it) must already be in place. Adding a role twice is harmless.
      final role = hereFor.role;
      if (role != null) await session.addRole(role);
      if (!ref.mounted) return;
      final account = await ref
          .read(accountRepositoryProvider)
          .updateDisplayName(raw.trim());
      if (!ref.mounted) return;
      // The router now sees a name and leaves this screen, which disposes this controller.
      session.accountUpdated(account);
    } on ApiFailure catch (e) {
      if (!ref.mounted) return;
      state = NameState(error: e.message);
    }
  }

  void clearError() {
    if (state.error != null) state = const NameState();
  }
}
