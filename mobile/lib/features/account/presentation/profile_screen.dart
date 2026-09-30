import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/theme/tokens.dart';
import '../../auth/presentation/auth_providers.dart';
import '../domain/account.dart';
import 'session_controller.dart';

/// Minimal profile: who the backend thinks you are. Instruments, hosting etc. come later.
class ProfileScreen extends ConsumerWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tokens = context.tokens;
    final textTheme = Theme.of(context).textTheme;
    final account = ref.watch(sessionControllerProvider.select((s) => s.value));
    if (account == null) {
      return const SizedBox.shrink(); // the router moves away
    }

    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(AppSpace.s5),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const SizedBox(height: AppSpace.s7),
              Text(
                'PROFILE',
                style: textTheme.labelSmall?.copyWith(
                  color: tokens.textTertiary,
                ),
              ),
              const SizedBox(height: AppSpace.s2),
              Text(
                account.displayName ?? 'No name yet',
                style: textTheme.displayMedium?.copyWith(
                  color: account.displayName == null
                      ? tokens.textTertiary
                      : null,
                ),
              ),
              const SizedBox(height: AppSpace.s2),
              Text(
                _formatPhone(account.phone),
                style: textTheme.bodyLarge?.copyWith(
                  color: tokens.textSecondary,
                  fontFeatures: const [FontFeature.tabularFigures()],
                ),
              ),
              const SizedBox(height: AppSpace.s5),
              Wrap(
                spacing: AppSpace.s2,
                runSpacing: AppSpace.s2,
                children: [
                  for (final role in Role.values)
                    if (account.roles.contains(role)) _RoleChip(role),
                ],
              ),
              const Spacer(),
              OutlinedButton(
                onPressed: () =>
                    ref.read(phoneAuthControllerProvider.notifier).signOut(),
                child: const Text('Sign out'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  static String _formatPhone(String? e164) {
    if (e164 == null) return '';
    if (e164.startsWith('+91') && e164.length == 13) {
      return '+91 ${e164.substring(3, 8)} ${e164.substring(8)}';
    }
    return e164;
  }
}

class _RoleChip extends StatelessWidget {
  const _RoleChip(this.role);

  final Role role;

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    final label = switch (role) {
      Role.user => 'Musician / listener',
      Role.host => 'Host',
      Role.venueAdmin => 'Venue admin',
    };
    return Container(
      height: 30,
      padding: const EdgeInsets.symmetric(horizontal: AppSpace.s3),
      alignment: Alignment.center,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(AppRadii.pill),
        border: Border.all(color: tokens.line),
      ),
      child: Text(
        label,
        style: Theme.of(context).textTheme.labelMedium
            ?.copyWith(color: tokens.textSecondary),
      ),
    );
  }
}
