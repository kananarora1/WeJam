import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/theme/tokens.dart';
import 'auth_providers.dart';

/// Interim landing screen for M1a: proves the Firebase sign-in. Replaced by profile/"me" in M1b.
class SignedInScreen extends ConsumerWidget {
  const SignedInScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final textTheme = Theme.of(context).textTheme;
    final user = ref.watch(authStateProvider.select((auth) => auth.value));

    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(AppSpace.s5),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const SizedBox(height: AppSpace.s7),
              Text("You're in", style: textTheme.displayMedium),
              const SizedBox(height: AppSpace.s6),
              _Field(label: 'PHONE', value: user?.phoneNumber ?? '—'),
              const SizedBox(height: AppSpace.s4),
              _Field(label: 'FIREBASE UID', value: user?.uid ?? '—'),
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
}

class _Field extends StatelessWidget {
  const _Field({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: textTheme.labelSmall?.copyWith(
            color: context.tokens.textTertiary,
          ),
        ),
        const SizedBox(height: AppSpace.s1),
        SelectableText(value, style: textTheme.bodyLarge),
      ],
    );
  }
}
