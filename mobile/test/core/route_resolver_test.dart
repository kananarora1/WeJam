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


  test('a ready session goes to profile', () {
    expect(resolve(), Routes.profile);
  });
}
