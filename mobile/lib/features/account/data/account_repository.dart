import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_error.dart';
import '../../../core/api/api_providers.dart';
import '../../../core/api/generated/export.dart';
import '../domain/account.dart';

final accountRepositoryProvider = Provider<AccountRepository>(
  (ref) => AccountRepository(ref.watch(apiProvider)),
);

/// The account endpoints, translated to domain types and [ApiFailure]s.
class AccountRepository {
  AccountRepository(this._api);

  final WeJamApi _api;

  /// Firebase ID token → app JWT.
  Future<String> exchange(String firebaseIdToken) => _call(() async {
    final response = await _api.auth.exchange(
      body: TokenRequest(firebaseIdToken: firebaseIdToken),
    );
    return response.accessToken;
  });

  Future<Account> me() => _call(() async => _toAccount(await _api.me.me()));

  /// [name] is sent as-is; the caller trims it (the backend trims and validates too).
  Future<Account> updateDisplayName(String name) => _call(
    () async => _toAccount(
      await _api.me.updateProfile(
        body: UpdateProfileRequest(displayName: name),
      ),
    ),
  );

  static Future<T> _call<T>(Future<T> Function() request) async {
    try {
      return await request();
    } on DioException catch (e) {
      throw ApiFailure.fromDio(e);
    }
  }

  static Account _toAccount(MeResponse me) => Account(
    id: me.id,
    phone: me.phone,
    displayName: me.displayName,
    roles: {
      for (final role in me.roles)
        ?switch (role) {
          MeResponseRoles.user => Role.user,
          MeResponseRoles.host => Role.host,
          MeResponseRoles.venueAdmin => Role.venueAdmin,
          MeResponseRoles.platformAdmin => Role.platformAdmin,
          // A role added on the backend after this app version shipped.
          MeResponseRoles.$unknown => null,
        },
    },
  );
}
