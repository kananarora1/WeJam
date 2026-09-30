import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/account/presentation/profile_screen.dart';
import '../../features/account/presentation/session_controller.dart';
import '../../features/account/presentation/session_error_screen.dart';
import '../../features/auth/presentation/auth_providers.dart';
import '../../features/auth/presentation/otp_screen.dart';
import '../../features/auth/presentation/phone_screen.dart';
import '../../features/auth/presentation/splash_screen.dart';
import '../theme/tokens.dart';
import '../widgets/loading_screen.dart';
import 'route_resolver.dart';

export 'route_resolver.dart' show Routes;

/// Navigation is derived from state (see [resolveRoute]), never pushed by screens.
final routerProvider = Provider<GoRouter>((ref) {
  // go_router re-runs `redirect` whenever this notifies; we bump it on every relevant state change.
  final refresh = ValueNotifier<int>(0);
  void bump(Object? _, Object? _) => refresh.value++;
  ref.listen(splashCompletedProvider, bump);
  ref.listen(authStateProvider, bump);
  ref.listen(phoneAuthControllerProvider.select((s) => s.awaitingCode), bump);
  ref.listen(sessionControllerProvider, bump);

  final router = GoRouter(
    initialLocation: Routes.splash,
    refreshListenable: refresh,
    redirect: (context, state) {
      final target = resolveRoute(
        splashCompleted: ref.read(splashCompletedProvider),
        auth: ref.read(authStateProvider),
        awaitingCode: ref.read(phoneAuthControllerProvider).awaitingCode,
        session: ref.read(sessionControllerProvider),
      );
      return state.matchedLocation == target ? null : target;
    },
    routes: [
      _fadeRoute(Routes.splash, const SplashScreen()),
      _fadeRoute(Routes.phone, const PhoneScreen()),
      _fadeRoute(Routes.otp, const OtpScreen()),
      _fadeRoute(
        Routes.loading,
        const LoadingScreen(message: 'Getting you in…'),
      ),
      _fadeRoute(Routes.sessionError, const SessionErrorScreen()),
      _fadeRoute(Routes.profile, const ProfileScreen()),
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
