import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_error.dart';
import '../data/account_repository.dart';
import 'session_controller.dart';

/// State of the "What should we call you?" form. Auto-disposed, so it starts fresh each visit.
final nameControllerProvider =
    NotifierProvider.autoDispose<NameController, NameState>(NameController.new);

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

  Future<void> submit(String raw) async {
    if (state.submitting) return;
    final invalid = validate(raw);
    if (invalid != null) {
      state = NameState(error: invalid);
      return;
    }

    state = const NameState(submitting: true);
    try {
      final account = await ref
          .read(accountRepositoryProvider)
          .updateDisplayName(raw.trim());
      if (!ref.mounted) return;
      // The router now sees a name and leaves this screen, which disposes this controller.
      ref.read(sessionControllerProvider.notifier).accountUpdated(account);
    } on ApiFailure catch (e) {
      if (!ref.mounted) return;
      state = NameState(error: e.message);
    }
  }

  void clearError() {
    if (state.error != null) state = const NameState();
  }
}
