import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wejam/core/api/api_error.dart';
import 'package:wejam/features/account/data/account_repository.dart';
import 'package:wejam/features/account/presentation/name_controller.dart';
import 'package:wejam/features/account/presentation/session_controller.dart';
import 'package:wejam/features/auth/data/phone_auth_service.dart';
import 'package:wejam/features/auth/domain/auth_models.dart';

import '../../support/account_fakes.dart';

void main() {
  late FakeAccounts accounts;
  late ProviderContainer container;

  NameController controller() =>
      container.read(nameControllerProvider.notifier);
  NameState state() => container.read(nameControllerProvider);

  setUp(() async {
    accounts = FakeAccounts();
    container = ProviderContainer.test(
      overrides: [
        phoneAuthServiceProvider.overrideWithValue(
          FakeAuth(const AuthUser(uid: 'u1')),
        ),
        accountRepositoryProvider.overrideWithValue(accounts),
      ],
    );
    // Keep both alive like the screen and router would.
    container.listen(sessionControllerProvider, (_, _) {});
    container.listen(nameControllerProvider, (_, _) {});
    await container.read(sessionControllerProvider.future);
  });

  test('trims the name, saves it and updates the session', () async {
    await controller().submit('  Rhea M.  ');

    expect(accounts.namesSent, ['Rhea M.']);
    expect(
      container.read(sessionControllerProvider).value?.displayName,
      'Rhea M.',
    );
  });

  test('a blank name is rejected without calling the backend', () async {
    await controller().submit('   ');

    expect(accounts.namesSent, isEmpty);
    expect(state().error, 'Enter the name you go by.');
  });

  test(
    'a name over 50 characters is rejected without calling the backend',
    () async {
      await controller().submit('a' * 51);

      expect(accounts.namesSent, isEmpty);
      expect(state().error, contains('50'));
    },
  );

  test('exactly 50 characters is accepted', () async {
    await controller().submit('a' * 50);

    expect(accounts.namesSent, ['a' * 50]);
  });

  test(
    'a backend failure shows its message and leaves the session unchanged',
    () async {
      accounts.updateError = const ApiFailure("Can't reach WeJam right now.");

      await controller().submit('Rhea');

      expect(state().submitting, isFalse);
      expect(state().error, "Can't reach WeJam right now.");
      expect(
        container.read(sessionControllerProvider).value?.displayName,
        isNull,
      );
    },
  );

  test('typing after an error clears it', () async {
    await controller().submit('');
    controller().clearError();

    expect(state().error, isNull);
  });
}
