import 'package:flutter/foundation.dart';

/// The signed-in Firebase user, without leaking Firebase types into the UI.
@immutable
class AuthUser {
  const AuthUser({required this.uid, this.phoneNumber});

  final String uid;
  final String? phoneNumber;
}

/// Outcome of asking Firebase to send an OTP.
sealed class SendCodeResult {
  const SendCodeResult();
}

/// SMS sent; the user must type the code.
class CodeSent extends SendCodeResult {
  const CodeSent({required this.verificationId, this.resendToken});

  final String verificationId;
  final int? resendToken;
}

/// Android verified the number by itself (auto-read SMS / instant verification) and already signed in.
class AutoVerified extends SendCodeResult {
  const AutoVerified();
}

/// A failure with copy that is safe to show the user as-is.
class AuthFailure implements Exception {
  const AuthFailure(this.message);

  final String message;

  @override
  String toString() => 'AuthFailure: $message';
}
