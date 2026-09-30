import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/theme/tokens.dart';
import '../../../core/widgets/primary_button.dart';
import '../../auth/presentation/auth_providers.dart';
import 'name_controller.dart';

/// Onboarding: shown by the router until the account has a display name.
class NameScreen extends ConsumerStatefulWidget {
  const NameScreen({super.key});

  @override
  ConsumerState<NameScreen> createState() => _NameScreenState();
}

class _NameScreenState extends ConsumerState<NameScreen> {
  final _name = TextEditingController();

  @override
  void dispose() {
    _name.dispose();
    super.dispose();
  }

  void _submit() =>
      ref.read(nameControllerProvider.notifier).submit(_name.text);

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    final textTheme = Theme.of(context).textTheme;
    final state = ref.watch(nameControllerProvider);

    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(
            AppSpace.s5,
            AppSpace.s8,
            AppSpace.s5,
            AppSpace.s5,
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text('What should we call you?', style: textTheme.displayMedium),
              const SizedBox(height: AppSpace.s3),
              Text(
                'This is how hosts and other musicians will see you.',
                style: textTheme.bodyLarge?.copyWith(
                  color: tokens.textSecondary,
                ),
              ),
              const SizedBox(height: AppSpace.s6),
              Text(
                'Your name',
                style: textTheme.labelMedium?.copyWith(
                  color: tokens.textSecondary,
                ),
              ),
              const SizedBox(height: AppSpace.s2),
              TextField(
                controller: _name,
                enabled: !state.submitting,
                autofocus: true,
                textCapitalization: TextCapitalization.words,
                textInputAction: TextInputAction.done,
                autofillHints: const [AutofillHints.name],
                inputFormatters: [
                  LengthLimitingTextInputFormatter(NameController.maxLength),
                ],
                style: textTheme.bodyLarge,
                decoration: const InputDecoration(hintText: 'e.g. Rhea M.'),
                onChanged: (_) =>
                    ref.read(nameControllerProvider.notifier).clearError(),
                onSubmitted: (_) => _submit(),
              ),
              if (state.error != null) ...[
                const SizedBox(height: AppSpace.s2),
                Text(
                  state.error!,
                  style: textTheme.bodyMedium?.copyWith(color: tokens.wineText),
                ),
              ],
              const Spacer(),
              ValueListenableBuilder<TextEditingValue>(
                valueListenable: _name,
                builder: (context, value, _) => PrimaryButton(
                  label: 'Continue',
                  loading: state.submitting,
                  onPressed: NameController.canSubmit(value.text)
                      ? _submit
                      : null,
                ),
              ),
              const SizedBox(height: AppSpace.s2),
              TextButton(
                onPressed: state.submitting
                    ? null
                    : () => ref
                          .read(phoneAuthControllerProvider.notifier)
                          .signOut(),
                child: const Text('Not you? Sign out'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
