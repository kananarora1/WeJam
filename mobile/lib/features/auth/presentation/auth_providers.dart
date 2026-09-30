import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/phone_auth_service.dart';
import '../domain/auth_models.dart';

/// Signed-in user, or null. Loading until Firebase restores (or not) the previous session.
final authStateProvider = StreamProvider<AuthUser?>(
  (ref) => ref.watch(phoneAuthServiceProvider).authStateChanges(),
);

final phoneAuthControllerProvider =
    NotifierProvider<PhoneAuthController, PhoneAuthState>(
      PhoneAuthController.new,
    );

enum PhoneAuthStep { enterPhone, sendingCode, enterCode, verifying }

@immutable
class PhoneAuthState {
  const PhoneAuthState({
    this.step = PhoneAuthStep.enterPhone,
    this.phoneE164,
    this.verificationId,
    this.resendToken,
    this.codeSentAt,
    this.error,
  });

  final PhoneAuthStep step;
  final String? phoneE164;
  final String? verificationId;
  final int? resendToken;
  final DateTime? codeSentAt;
  final String? error;

  bool get awaitingCode => verificationId != null;

  PhoneAuthState copyWith({
    PhoneAuthStep? step,
    String? phoneE164,
    String? verificationId,
    int? resendToken,
    DateTime? codeSentAt,
    String? error,
    bool clearError = false,
  }) {
    return PhoneAuthState(
      step: step ?? this.step,
      phoneE164: phoneE164 ?? this.phoneE164,
      verificationId: verificationId ?? this.verificationId,
      resendToken: resendToken ?? this.resendToken,
      codeSentAt: codeSentAt ?? this.codeSentAt,
      error: clearError ? null : (error ?? this.error),
    );
  }
}

/// The phone → code flow. Navigation is not done here: the router reacts to [PhoneAuthState.awaitingCode]
/// and to [authStateProvider].
class PhoneAuthController extends Notifier<PhoneAuthState> {
  static const countryCode = '+91';
  static const localNumberLength = 10;
  static const codeLength = 6;

  @override
  PhoneAuthState build() => const PhoneAuthState();

  PhoneAuthService get _service => ref.read(phoneAuthServiceProvider);

  static bool isValidLocalNumber(String digits) =>
      RegExp(r'^\d{10}$').hasMatch(digits);

  Future<void> sendCode(String localDigits) async {
    if (state.step == PhoneAuthStep.sendingCode) return;
    if (!isValidLocalNumber(localDigits)) {
      state = state.copyWith(error: 'Enter a 10-digit mobile number.');
      return;
    }
    final phone = '$countryCode$localDigits';
    state = state.copyWith(
      step: PhoneAuthStep.sendingCode,
      phoneE164: phone,
      clearError: true,
    );
    await _send(phone, resendToken: null);
  }

  Future<void> resend() async {
    final phone = state.phoneE164;
    if (phone == null || state.step == PhoneAuthStep.sendingCode) return;
    state = state.copyWith(step: PhoneAuthStep.sendingCode, clearError: true);
    await _send(phone, resendToken: state.resendToken);
  }

  Future<void> verify(String code) async {
    final verificationId = state.verificationId;
    if (verificationId == null || state.step == PhoneAuthStep.verifying) return;
    if (code.length != codeLength) return;

    state = state.copyWith(step: PhoneAuthStep.verifying, clearError: true);
    try {
      await _service.verifyCode(verificationId: verificationId, smsCode: code);
      if (!ref.mounted) return;
      state = const PhoneAuthState();
    } on AuthFailure catch (e) {
      if (!ref.mounted) return;
      state = state.copyWith(step: PhoneAuthStep.enterCode, error: e.message);
    }
  }

  /// Back from the code screen; keeps the number so the field is pre-filled.
  void editNumber() => state = PhoneAuthState(phoneE164: state.phoneE164);

  Future<void> signOut() async {
    await _service.signOut();
    if (!ref.mounted) return;
    state = const PhoneAuthState();
  }

  Future<void> _send(String phone, {required int? resendToken}) async {
    try {
      final result = await _service.sendCode(phone, resendToken: resendToken);
      if (!ref.mounted) return;
      state = switch (result) {
        CodeSent(:final verificationId, :final resendToken) => state.copyWith(
          step: PhoneAuthStep.enterCode,
          verificationId: verificationId,
          resendToken: resendToken,
          codeSentAt: DateTime.now(),
        ),
        AutoVerified() => const PhoneAuthState(),
      };
    } on AuthFailure catch (e) {
      if (!ref.mounted) return;
      state = state.copyWith(
        step: state.awaitingCode
            ? PhoneAuthStep.enterCode
            : PhoneAuthStep.enterPhone,
        error: e.message,
      );
    }
  }
}
