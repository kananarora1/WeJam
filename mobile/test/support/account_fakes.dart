import 'package:wejam/features/account/data/account_repository.dart';
import 'package:wejam/features/account/domain/account.dart';
import 'package:wejam/features/auth/data/phone_auth_service.dart';
import 'package:wejam/features/auth/domain/auth_models.dart';

class FakeAuth implements PhoneAuthService {
  FakeAuth(this.user);

  AuthUser? user;
  final forceRefreshes = <bool>[];
  var signOuts = 0;

  @override
  Stream<AuthUser?> authStateChanges() => Stream.value(user);

  @override
  Future<String> getIdToken({bool forceRefresh = false}) async {
    forceRefreshes.add(forceRefresh);
    return forceRefresh ? 'fresh-id-token' : 'id-token';
  }

  @override
  Future<void> signOut() async => signOuts++;

  @override
  Future<SendCodeResult> sendCode(String phoneE164, {int? resendToken}) =>
      throw UnimplementedError();

  @override
  Future<void> verifyCode({
    required String verificationId,
    required String smsCode,
  }) => throw UnimplementedError();
}

class FakeAccounts implements AccountRepository {
  Object? exchangeError;
  final exchanged = <String>[];

  static const account = Account(
    id: 'a1',
    phone: '+911234567890',
    displayName: null,
    roles: {Role.user},
  );

  @override
  Future<String> exchange(String firebaseIdToken) async {
    exchanged.add(firebaseIdToken);
    await Future<void>.delayed(Duration.zero);
    if (exchangeError != null) throw exchangeError!;
    return 'jwt-for-$firebaseIdToken';
  }

  /// The roles the backend currently has for this user.
  final roles = {...account.roles};
  String? displayName;

  /// Every write, in order — e.g. `['addRole:venueAdmin', 'name:Rhea']`.
  final calls = <String>[];

  /// The shared [account] until something has been written, so tests can check identity.
  @override
  Future<Account> me() async => calls.isEmpty ? account : _current();

  Object? updateError;
  final namesSent = <String>[];

  @override
  Future<Account> updateDisplayName(String name) async {
    namesSent.add(name);
    calls.add('name:$name');
    await Future<void>.delayed(Duration.zero);
    if (updateError != null) throw updateError!;
    displayName = name;
    return _current();
  }

  Object? addRoleError;

  @override
  Future<String> addRole(Role role) async {
    calls.add('addRole:${role.name}');
    await Future<void>.delayed(Duration.zero);
    if (addRoleError != null) throw addRoleError!;
    roles.add(role);
    return 'jwt-with-${role.name}';
  }

  Account _current() => Account(
    id: account.id,
    phone: account.phone,
    displayName: displayName ?? account.displayName,
    roles: {...roles},
  );
}
