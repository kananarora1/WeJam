import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wejam/features/auth/data/phone_auth_service.dart';
import 'package:wejam/features/auth/domain/auth_models.dart';
import 'package:wejam/features/auth/presentation/auth_providers.dart';

class FakePhoneAuthService implements PhoneAuthService {
  SendCodeResult sendResult = const CodeSent(
    verificationId: 'vid-1',
    resendToken: 7,
  );
  AuthFailure? sendFailure;
  String validCode = '123456';

  final sentTo = <String>[];
  final resendTokens = <int?>[];
  var verifyCalls = 0;

  @override
  Stream<AuthUser?> authStateChanges() => const Stream.empty();

  @override
  Future<SendCodeResult> sendCode(String phoneE164, {int? resendToken}) async {
    sentTo.add(phoneE164);
    resendTokens.add(resendToken);
    if (sendFailure != null) throw sendFailure!;
    return sendResult;
  }

  @override
  Future<void> verifyCode({
    required String verificationId,
    required String smsCode,
  }) async {
    verifyCalls++;
    if (smsCode != validCode) throw const AuthFailure('wrong code');
  }

  @override
  Future<void> signOut() async {}
}

void main() {
  late FakePhoneAuthService fake;
  late ProviderContainer container;

  PhoneAuthController controller() =>
      container.read(phoneAuthControllerProvider.notifier);
  PhoneAuthState state() => container.read(phoneAuthControllerProvider);

  setUp(() {
    fake = FakePhoneAuthService();
    container = ProviderContainer.test(
      overrides: [phoneAuthServiceProvider.overrideWithValue(fake)],
    );
  });

  test(
    'sending a valid number moves to enterCode with the verification id',
    () async {
      await controller().sendCode('1234567890');

      expect(fake.sentTo, ['+911234567890']);
      expect(state().step, PhoneAuthStep.enterCode);
      expect(state().verificationId, 'vid-1');
      expect(state().awaitingCode, isTrue);
      expect(state().codeSentAt, isNotNull);
      expect(state().error, isNull);
    },
  );

  test('a short number is rejected without calling Firebase', () async {
    await controller().sendCode('12345');

    expect(fake.sentTo, isEmpty);
    expect(state().step, PhoneAuthStep.enterPhone);
    expect(state().error, 'Enter a 10-digit mobile number.');
  });

  test('a send failure stays on the phone step with the message', () async {
    fake.sendFailure = const AuthFailure('No connection.');

    await controller().sendCode('1234567890');

    expect(state().step, PhoneAuthStep.enterPhone);
    expect(state().awaitingCode, isFalse);
    expect(state().error, 'No connection.');
  });

  test(
    'auto-verification resets the flow (the auth stream takes over)',
    () async {
      fake.sendResult = const AutoVerified();

      await controller().sendCode('1234567890');

      expect(state().step, PhoneAuthStep.enterPhone);
      expect(state().awaitingCode, isFalse);
    },
  );

  test(
    'a wrong code stays on enterCode with an error; the right code resets',
    () async {
      await controller().sendCode('1234567890');

      await controller().verify('000000');
      expect(state().step, PhoneAuthStep.enterCode);
      expect(state().error, 'wrong code');

      await controller().verify('123456');
      expect(state().step, PhoneAuthStep.enterPhone);
      expect(state().awaitingCode, isFalse);
      expect(state().error, isNull);
    },
  );

  test('incomplete codes are not sent for verification', () async {
    await controller().sendCode('1234567890');

    await controller().verify('123');

    expect(fake.verifyCalls, 0);
  });

  test('resend reuses the resend token from the first send', () async {
    await controller().sendCode('1234567890');

    await controller().resend();

    expect(fake.resendTokens, [null, 7]);
    expect(state().step, PhoneAuthStep.enterCode);
  });

  test(
    'editing the number leaves the code step but keeps the number',
    () async {
      await controller().sendCode('1234567890');

      controller().editNumber();

      expect(state().awaitingCode, isFalse);
      expect(state().phoneE164, '+911234567890');
    },
  );
}
