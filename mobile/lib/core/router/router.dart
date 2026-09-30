import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/auth/presentation/auth_providers.dart';
import '../../features/auth/presentation/otp_screen.dart';
import '../../features/auth/presentation/phone_screen.dart';
import '../../features/auth/presentation/signed_in_screen.dart';
import '../../features/auth/presentation/splash_screen.dart';
import '../theme/tokens.dart';

abstract final class Routes {
  static const splash = '/splash';
  static const phone = '/phone';
  static const otp = '/otp';
  static const home = '/home';
}

/// Navigation is derived from state, never pushed by screens:
/// splash until it has played (it waits for the session check itself); signed out → phone
/// (or otp once a code was sent); signed in → home.
final routerProvider = Provider<GoRouter>((ref) {
  // go_router re-runs `redirect` whenever this notifies; we bump it on every relevant state change.
  final refresh = ValueNotifier<int>(0);
  ref.listen(splashCompletedProvider, (_, _) => refresh.value++);
  ref.listen(authStateProvider, (_, _) => refresh.value++);
  ref.listen(
    phoneAuthControllerProvider.select((s) => s.awaitingCode),
    (_, _) => refresh.value++,
  );

  final router = GoRouter(
    initialLocation: Routes.splash,
    refreshListenable: refresh,
    redirect: (context, state) => _redirect(ref, state.matchedLocation),
    routes: [
      _fadeRoute(Routes.splash, const SplashScreen()),
      _fadeRoute(Routes.phone, const PhoneScreen()),
      _fadeRoute(Routes.otp, const OtpScreen()),
      _fadeRoute(Routes.home, const SignedInScreen()),
    ],
  );

  ref.onDispose(() {
    router.dispose();
    refresh.dispose();
  });
  return router;
});

/// Calm cross-fade between screens. The platform default (a zoom on Android) would fight the
/// splash's own pop-out.
GoRoute _fadeRoute(String path, Widget screen) => GoRoute(
  path: path,
  pageBuilder: (_, state) => CustomTransitionPage<void>(
    key: state.pageKey,
    child: screen,
    transitionDuration: AppMotion.standard,
    reverseTransitionDuration: AppMotion.standard,
    transitionsBuilder: (_, animation, _, child) => FadeTransition(
      opacity: CurvedAnimation(parent: animation, curve: AppMotion.easeOut),
      child: child,
    ),
  ),
);

String? _redirect(Ref ref, String location) {
  if (!ref.read(splashCompletedProvider)) return Routes.splash;
  final auth = ref.read(authStateProvider);
  if (auth.isLoading && !auth.hasValue) return Routes.splash;

  // An error reading the session is treated as signed out: the user can always log in again.
  final signedIn = auth.value != null;
  final target = signedIn
      ? Routes.home
      : ref.read(phoneAuthControllerProvider).awaitingCode
      ? Routes.otp
      : Routes.phone;
  return location == target ? null : target;
}
