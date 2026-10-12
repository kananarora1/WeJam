import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/api/api_error.dart';
import '../../../core/router/route_resolver.dart';
import '../../../core/theme/tokens.dart';
import '../../../core/widgets/eq_bars.dart';
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
              _RoleActions(account: account),
              const SizedBox(height: AppSpace.s3),
              TextButton(
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

/// "Role is changeable later" (design 1.4): turn on running a venue or hosting from here.
class _RoleActions extends ConsumerStatefulWidget {
  const _RoleActions({required this.account});

  final Account account;

  @override
  ConsumerState<_RoleActions> createState() => _RoleActionsState();
}

class _RoleActionsState extends ConsumerState<_RoleActions> {
  Role? _adding;
  String? _error;

  Future<void> _add(Role role) async {
    setState(() {
      _adding = role;
      _error = null;
    });
    try {
      await ref.read(sessionControllerProvider.notifier).addRole(role);
      if (!mounted) return;
      if (role == Role.venueAdmin) {
        context.go(Routes.venue);
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Hosting is on. Your host profile comes next.'),
          ),
        );
      }
    } on ApiFailure catch (e) {
      if (mounted) setState(() => _error = e.message);
    } finally {
      if (mounted) setState(() => _adding = null);
    }
  }

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    final roles = widget.account.roles;
    Widget action(String label, VoidCallback onPressed, {Role? adds}) =>
        OutlinedButton(
          onPressed: _adding == null ? onPressed : null,
          child: adds != null && _adding == adds
              ? EqBars(color: tokens.textSecondary)
              : Text(label),
        );

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        if (roles.contains(Role.venueAdmin))
          action('My venue', () => context.go(Routes.venue))
        else
          action(
            'Run a venue',
            () => _add(Role.venueAdmin),
            adds: Role.venueAdmin,
          ),
        if (!roles.contains(Role.host)) ...[
          const SizedBox(height: AppSpace.s2),
          action('Start hosting', () => _add(Role.host), adds: Role.host),
        ],
        if (_error != null) ...[
          const SizedBox(height: AppSpace.s2),
          Text(
            _error!,
            style: Theme.of(context).textTheme.bodyMedium
                ?.copyWith(color: tokens.wineText),
          ),
        ],
      ],
    );
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
      Role.platformAdmin => 'Platform admin',
    };
    return Container(
      height: 30,
      padding: const EdgeInsets.symmetric(horizontal: AppSpace.s3),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(AppRadii.pill),
        border: Border.all(color: tokens.line),
      ),
      // widthFactor 1: as wide as the label (`alignment` alone would fill the row).
      child: Center(
        widthFactor: 1,
        child: Text(
          label,
          style: Theme.of(context).textTheme.labelMedium
              ?.copyWith(color: tokens.textSecondary),
        ),
      ),
    );
  }
}
