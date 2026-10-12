import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../features/account/domain/account.dart';
import '../../features/auth/domain/auth_models.dart';

abstract final class Routes {
  static const splash = '/splash';
  static const phone = '/phone';
  static const otp = '/otp';
  static const loading = '/loading';
  static const sessionError = '/session-error';
  static const name = '/name';
  static const profile = '/profile';
  static const venue = '/venue';
  static const newVenue = '/venue/new';
  static String venueDetails(String venueId) => '/venue/$venueId/details';

  /// Where a signed-in, onboarded user lands: venue owners start at their venue.
  static String home(Account account) =>
      account.roles.contains(Role.venueAdmin) ? venue : profile;
}

/// Past the gates (signed in, session ready, named), these screens and everything under them can be
/// visited freely; before that, only the gate's own screen is allowed.
const _signedInAreas = [Routes.profile, Routes.venue];

/// Pure so the rules are unit-testable: null = stay at [location].
String? redirectFor({required String location, required String target}) {
  if (location == target) return null;
  final pastTheGates = target == Routes.profile || target == Routes.venue;
  final inSignedInArea = _signedInAreas.any(
    (area) => location == area || location.startsWith('$area/'),
  );
  return pastTheGates && inSignedInArea ? null : target;
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
  final account = session.value;
  if (account == null) return Routes.loading;
  // Nobody gets past onboarding without a name, even after closing the app halfway.
  if (account.displayName == null) return Routes.name;
  return Routes.home(account);
}
