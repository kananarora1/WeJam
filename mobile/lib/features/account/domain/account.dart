import 'package:flutter/foundation.dart';

enum Role { user, host, venueAdmin, platformAdmin }

/// The signed-in user's profile as the app uses it (decoupled from the generated DTOs).
@immutable
class Account {
  const Account({
    required this.id,
    required this.phone,
    required this.displayName,
    required this.roles,
  });

  final String id;
  final String? phone;

  /// Null until the user completes onboarding.
  final String? displayName;
  final Set<Role> roles;
}
