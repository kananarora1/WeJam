import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/api/api_error.dart';
import '../../../core/theme/tokens.dart';
import '../../../core/widgets/primary_button.dart';
import '../../auth/domain/auth_models.dart';
import '../../auth/presentation/auth_providers.dart';
import 'session_controller.dart';

/// Signed in to Firebase, but the backend session couldn't be set up (backend down, no network…).
class SessionErrorScreen extends ConsumerStatefulWidget {
  const SessionErrorScreen({super.key});

  @override
  ConsumerState<SessionErrorScreen> createState() => _SessionErrorScreenState();
}

class _SessionErrorScreenState extends ConsumerState<SessionErrorScreen> {
  bool _retrying = false;

  Future<void> _retry() async {
    setState(() => _retrying = true);
    await ref.read(sessionControllerProvider.notifier).retry();
    if (mounted) setState(() => _retrying = false);
  }

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    final textTheme = Theme.of(context).textTheme;
    final error = ref.watch(sessionControllerProvider.select((s) => s.error));
    final message = switch (error) {
      ApiFailure(:final message) || AuthFailure(:final message) => message,
      _ => "We couldn't load your account just now. It's us, not you — try again in a moment.",
    };

    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(AppSpace.s5),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Spacer(),
              Text("That didn't land", style: textTheme.displayMedium),
              const SizedBox(height: AppSpace.s3),
              Text(
                message,
                style: textTheme.bodyLarge?.copyWith(
                  color: tokens.textSecondary,
                ),
              ),
              const Spacer(),
              PrimaryButton(
                label: 'Try again',
                loading: _retrying,
                onPressed: _retry,
              ),
              const SizedBox(height: AppSpace.s3),
              TextButton(
                onPressed: _retrying
                    ? null
                    : () => ref
                          .read(phoneAuthControllerProvider.notifier)
                          .signOut(),
                child: const Text('Sign out'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
