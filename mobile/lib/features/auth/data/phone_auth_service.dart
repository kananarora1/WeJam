import 'dart:async';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../domain/auth_models.dart';

/// Phone sign-in. An interface so tests can swap in a fake instead of talking to Firebase.
abstract interface class PhoneAuthService {
  Stream<AuthUser?> authStateChanges();

  /// [phoneE164] like "+911234567890". Pass the previous [resendToken] to resend.
  Future<SendCodeResult> sendCode(String phoneE164, {int? resendToken});

  Future<void> verifyCode({
    required String verificationId,
    required String smsCode,
  });

  Future<void> signOut();
}

final phoneAuthServiceProvider = Provider<PhoneAuthService>(
  (ref) => FirebasePhoneAuthService(FirebaseAuth.instance),
);

class FirebasePhoneAuthService implements PhoneAuthService {
  FirebasePhoneAuthService(this._auth);

  final FirebaseAuth _auth;

  @override
  Stream<AuthUser?> authStateChanges() => _auth.authStateChanges().map(
    (user) => user == null
        ? null
        : AuthUser(uid: user.uid, phoneNumber: user.phoneNumber),
  );

  @override
  Future<SendCodeResult> sendCode(String phoneE164, {int? resendToken}) {
    // verifyPhoneNumber reports through callbacks; a Completer turns the first one into a Future.
    final result = Completer<SendCodeResult>();

    void fail(Object error) {
      if (!result.isCompleted) result.completeError(_toFailure(error));
    }

    _auth
        .verifyPhoneNumber(
          phoneNumber: phoneE164,
          forceResendingToken: resendToken,
          verificationCompleted: (credential) async {
            // Can also fire after codeSent (SMS auto-read): signing in then flips authStateChanges.
            try {
              await _auth.signInWithCredential(credential);
              if (!result.isCompleted) result.complete(const AutoVerified());
            } catch (e) {
              fail(e);
            }
          },
          verificationFailed: fail,
          codeSent: (verificationId, token) {
            if (!result.isCompleted) {
              result.complete(
                CodeSent(verificationId: verificationId, resendToken: token),
              );
            }
          },
          codeAutoRetrievalTimeout: (_) {},
        )
        .catchError(fail);

    return result.future;
  }

  @override
  Future<void> verifyCode({
    required String verificationId,
    required String smsCode,
  }) async {
    try {
      final credential = PhoneAuthProvider.credential(
        verificationId: verificationId,
        smsCode: smsCode,
      );
      await _auth.signInWithCredential(credential);
    } catch (e) {
      throw _toFailure(e);
    }
  }

  @override
  Future<void> signOut() => _auth.signOut();

  static AuthFailure _toFailure(Object error) {
    // The user only sees friendly copy, so keep the raw cause visible in `flutter run` output.
    debugPrint('Phone auth failed: $error');
    if (error is! FirebaseAuthException) {
      return const AuthFailure(
        'Something went wrong on our side. Try again in a moment.',
      );
    }
    // Firebase reports this as a generic "internal-error"; only the message names it.
    // It means real SMS needs the paid plan (see ADR-012) — use a test number.
    if (error.message?.contains('BILLING_NOT_ENABLED') ?? false) {
      return const AuthFailure(
        "SMS sign-in isn't enabled for this number yet. Try again later.",
      );
    }
    return AuthFailure(switch (error.code) {
      'invalid-phone-number' =>
        "That number doesn't look right. Check it and try again.",
      'invalid-verification-code' =>
        "That code didn't match. Check the SMS and try again.",
      'session-expired' ||
      'code-expired' => 'That code has expired. Tap resend for a new one.',
      'too-many-requests' =>
        'Too many attempts. Wait a few minutes and try again.',
      'network-request-failed' =>
        'No connection. Check your internet and try again.',
      'operation-not-allowed' =>
        "Phone sign-in isn't available for this region yet.",
      _ => 'Something went wrong on our side. Try again in a moment.',
    });
  }
}
