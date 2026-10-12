import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wejam/core/api/api_error.dart';
import 'package:wejam/core/router/route_resolver.dart';
import 'package:wejam/features/account/domain/account.dart';
import 'package:wejam/features/auth/domain/auth_models.dart';

void main() {
  const user = AuthUser(uid: 'u1', phoneNumber: '+911234567890');
  const account = Account(
    id: 'a1',
    phone: '+911234567890',
    displayName: 'Bob',
    roles: {Role.user},
  );

  String resolve({
    bool splashCompleted = true,
    AsyncValue<AuthUser?> auth = const AsyncData(user),
    bool awaitingCode = false,
    AsyncValue<Account?> session = const AsyncData(account),
  }) => resolveRoute(
    splashCompleted: splashCompleted,
    auth: auth,
    awaitingCode: awaitingCode,
    session: session,
  );

  test('splash until it has played, whatever else is known', () {
    expect(resolve(splashCompleted: false), Routes.splash);
  });

  test('splash while the Firebase session is still unknown', () {
    expect(resolve(auth: const AsyncLoading()), Routes.splash);
  });

  test('signed out goes to phone, or otp once a code is pending', () {
    expect(resolve(auth: const AsyncData(null)), Routes.phone);
    expect(
      resolve(auth: const AsyncData(null), awaitingCode: true),
      Routes.otp,
    );
  });

  test('a failed Firebase session read counts as signed out', () {
    expect(
      resolve(auth: AsyncError<AuthUser?>(Exception('x'), StackTrace.empty)),
      Routes.phone,
    );
  });

  test(
    'signed in while the backend session loads shows the loading screen',
    () {
      expect(resolve(session: const AsyncLoading()), Routes.loading);
      expect(resolve(session: const AsyncData(null)), Routes.loading);
    },
  );

  test('a backend session failure shows the error screen', () {
    expect(
      resolve(
        session: const AsyncError<Account?>(
          ApiFailure('down'),
          StackTrace.empty,
        ),
      ),
      Routes.sessionError,
    );
  });

  test('a ready session without a name goes to the name screen', () {
    const unnamed = Account(
      id: 'a1',
      phone: '+911234567890',
      displayName: null,
      roles: {Role.user},
    );
    expect(resolve(session: const AsyncData(unnamed)), Routes.name);
  });

  test('a ready session with a name goes to profile', () {
    expect(resolve(), Routes.profile);
  });

  test('venue owners land on their venue', () {
    const owner = Account(
      id: 'a1',
      phone: '+911234567890',
      displayName: 'Bob',
      roles: {Role.user, Role.venueAdmin},
    );
    expect(resolve(session: const AsyncData(owner)), Routes.venue);
  });

  group('redirectFor', () {
    test('past the gates, any signed-in screen can be visited', () {
      for (final location in [
        Routes.profile,
        Routes.venue,
        Routes.newVenue,
        Routes.venueDetails('v1'),
      ]) {
        expect(redirectFor(location: location, target: Routes.profile), isNull);
        expect(redirectFor(location: location, target: Routes.venue), isNull);
      }
    });

    test('a gate always wins over a signed-in screen', () {
      expect(
        redirectFor(location: Routes.newVenue, target: Routes.phone),
        Routes.phone,
      );
      expect(
        redirectFor(location: Routes.profile, target: Routes.name),
        Routes.name,
      );
      expect(
        redirectFor(location: Routes.venue, target: Routes.sessionError),
        Routes.sessionError,
      );
    });

    test('a gate screen is left once its gate is passed', () {
      expect(
        redirectFor(location: Routes.name, target: Routes.venue),
        Routes.venue,
      );
      expect(
        redirectFor(location: Routes.otp, target: Routes.profile),
        Routes.profile,
      );
    });

    test('a path that only starts like an area is not inside it', () {
      expect(
        redirectFor(location: '/venues-old', target: Routes.profile),
        Routes.profile,
      );
    });
  });
}
