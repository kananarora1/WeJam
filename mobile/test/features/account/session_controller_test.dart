import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wejam/core/api/api_error.dart';
import 'package:wejam/core/api/app_token_store.dart';
import 'package:wejam/features/account/data/account_repository.dart';
import 'package:wejam/features/account/presentation/session_controller.dart';
import 'package:wejam/features/auth/data/phone_auth_service.dart';
import 'package:wejam/features/auth/domain/auth_models.dart';

import '../../support/account_fakes.dart';

void main() {
  late FakeAuth auth;
  late FakeAccounts accounts;
  late ProviderContainer container;

  void setUpWith(AuthUser? user) {
    auth = FakeAuth(user);
    accounts = FakeAccounts();
    container = ProviderContainer.test(
      overrides: [
        phoneAuthServiceProvider.overrideWithValue(auth),
        accountRepositoryProvider.overrideWithValue(accounts),
      ],
    );
    container.listen(sessionControllerProvider, (_, _) {});
  }

  String? token() => container.read(appTokenStoreProvider).accessToken;

  test('signed out: no session and no app JWT', () async {
    setUpWith(null);

    expect(await container.read(sessionControllerProvider.future), isNull);
    expect(accounts.exchanged, isEmpty);
    expect(token(), isNull);
  });

  test(
    'signed in: exchanges the Firebase token, stores the JWT, loads /me',
    () async {
      setUpWith(const AuthUser(uid: 'u1'));

      final account = await container.read(sessionControllerProvider.future);

      expect(account, same(FakeAccounts.account));
      expect(accounts.exchanged, ['id-token']);
      expect(token(), 'jwt-for-id-token');
    },
  );

  test(
    'a failed exchange surfaces as an error (not retried silently)',
    () async {
      setUpWith(const AuthUser(uid: 'u1'));
      accounts.exchangeError = const ApiFailure("Can't reach WeJam right now.");

      await expectLater(
        container.read(sessionControllerProvider.future),
        throwsA(isA<ApiFailure>()),
      );
      expect(accounts.exchanged, hasLength(1));
    },
  );

  test(
    'reauthenticate forces a fresh Firebase token and replaces the JWT',
    () async {
      setUpWith(const AuthUser(uid: 'u1'));
      await container.read(sessionControllerProvider.future);

      final ok = await container
          .read(sessionControllerProvider.notifier)
          .reauthenticate();

      expect(ok, isTrue);
      expect(auth.forceRefreshes.last, isTrue);
      expect(token(), 'jwt-for-fresh-id-token');
    },
  );

  test('concurrent 401s share one re-authentication', () async {
    setUpWith(const AuthUser(uid: 'u1'));
    await container.read(sessionControllerProvider.future);
    final notifier = container.read(sessionControllerProvider.notifier);

    final results = await Future.wait([
      notifier.reauthenticate(),
      notifier.reauthenticate(),
      notifier.reauthenticate(),
    ]);

    expect(results, [true, true, true]);
    expect(accounts.exchanged, ['id-token', 'fresh-id-token']);
  });

  test('a rejected fresh token signs the user out', () async {
    setUpWith(const AuthUser(uid: 'u1'));
    await container.read(sessionControllerProvider.future);
    accounts.exchangeError = const ApiFailure('ended', statusCode: 401);

    final ok = await container
        .read(sessionControllerProvider.notifier)
        .reauthenticate();

    expect(ok, isFalse);
    expect(auth.signOuts, 1);
  });

  test(
    'a network failure during re-authentication does not sign out',
    () async {
      setUpWith(const AuthUser(uid: 'u1'));
      await container.read(sessionControllerProvider.future);
      accounts.exchangeError = const ApiFailure("Can't reach WeJam right now.");

      final ok = await container
          .read(sessionControllerProvider.notifier)
          .reauthenticate();

      expect(ok, isFalse);
      expect(auth.signOuts, 0);
    },
  );
}
