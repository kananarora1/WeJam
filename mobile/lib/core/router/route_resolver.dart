import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../features/account/domain/account.dart';
import '../../features/auth/domain/auth_models.dart';

abstract final class Routes {
  static const splash = '/splash';
  static const phone = '/phone';
  static const otp = '/otp';
  static const loading = '/loading';
  static const sessionError = '/session-error';
  static const profile = '/profile';
}

/// Where the user must be, given the app's state. Pure so the rules are unit-testable.
String resolveRoute({
  required bool splashCompleted,
  required AsyncValue<AuthUser?> auth,
  required bool awaitingCode,
  required AsyncValue<Account?> session,
}) {
  if (!splashCompleted) return Routes.splash;
  if (auth.isLoading && !auth.hasValue) return Routes.splash;

  // An error reading the Firebase session is treated as signed out: the user can log in again.
  if (auth.value == null) return awaitingCode ? Routes.otp : Routes.phone;

  // Loading is checked before error: a retry keeps the previous error while it reloads.
  if (session.isLoading) return Routes.loading;
  if (session.hasError) return Routes.sessionError;
  // Signed in to Firebase but the session hasn't started building yet.
  if (session.value == null) return Routes.loading;
  return Routes.profile;
}
