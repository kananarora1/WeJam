import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/theme/tokens.dart';
import '../../../core/widgets/primary_button.dart';
import '../../auth/presentation/auth_providers.dart';
import 'name_controller.dart';

/// Onboarding (design 1.4): shown by the router until the account has a display name.
/// The avatar and the next onboarding steps (1.5 instruments, 1.6 location) come later.
class NameScreen extends ConsumerStatefulWidget {
  const NameScreen({super.key});

  @override
  ConsumerState<NameScreen> createState() => _NameScreenState();
}

class _NameScreenState extends ConsumerState<NameScreen> {
  final _name = TextEditingController();
  var _hereFor = HereFor.playOrListen;

  @override
  void dispose() {
    _name.dispose();
    super.dispose();
  }

  void _submit() => ref
      .read(nameControllerProvider.notifier)
      .submit(_name.text, hereFor: _hereFor);

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    final textTheme = Theme.of(context).textTheme;
    final state = ref.watch(nameControllerProvider);

    return Scaffold(
      body: SafeArea(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.fromLTRB(
                  AppSpace.s5,
                  AppSpace.s8,
                  AppSpace.s5,
                  AppSpace.s5,
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Text(
                      "Who's coming tonight?",
                      style: textTheme.displayMedium,
                    ),
                    const SizedBox(height: AppSpace.s6),
                    Text(
                      'Name',
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
                        LengthLimitingTextInputFormatter(
                          NameController.maxLength,
                        ),
                      ],
                      style: textTheme.bodyLarge,
                      decoration: const InputDecoration(
                        hintText: 'e.g. Rhea M.',
                      ),
                      onChanged: (_) => ref
                          .read(nameControllerProvider.notifier)
                          .clearError(),
                      onSubmitted: (_) => _submit(),
                    ),
                    if (state.error != null) ...[
                      const SizedBox(height: AppSpace.s2),
                      Text(
                        state.error!,
                        style: textTheme.bodyMedium?.copyWith(
                          color: tokens.wineText,
                        ),
                      ),
                    ],
                    const SizedBox(height: AppSpace.s6),
                    Text(
                      "I'M HERE TO",
                      style: textTheme.labelSmall?.copyWith(
                        color: tokens.textTertiary,
                      ),
                    ),
                    const SizedBox(height: AppSpace.s3),
                    for (final option in HereFor.values) ...[
                      _HereForCard(
                        option: option,
                        selected: option == _hereFor,
                        enabled: !state.submitting,
                        onTap: () => setState(() => _hereFor = option),
                      ),
                      const SizedBox(height: AppSpace.s3 - 2),
                    ],
                  ],
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(
                AppSpace.s5,
                AppSpace.s2,
                AppSpace.s5,
                AppSpace.s4,
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
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
                  const SizedBox(height: AppSpace.s1),
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
          ],
        ),
      ),
    );
  }
}

/// One choice of "I'm here to": amber rim, tint and filled check when selected.
class _HereForCard extends StatelessWidget {
  const _HereForCard({
    required this.option,
    required this.selected,
    required this.enabled,
    required this.onTap,
  });

  final HereFor option;
  final bool selected;
  final bool enabled;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    final textTheme = Theme.of(context).textTheme;
    final (icon, title, subtitle) = switch (option) {
      HereFor.playOrListen => (
        Icons.headphones_outlined,
        'Play or listen',
        'Find jams, join as performer or audience',
      ),
      HereFor.hostJams => (
        Icons.mic_none_outlined,
        'Host jams',
        'Propose nights at venues, run the lineup',
      ),
      HereFor.runAVenue => (
        Icons.storefront_outlined,
        'Run a venue',
        'List spaces, approve requests',
      ),
    };

    return Semantics(
      inMutuallyExclusiveGroup: true,
      selected: selected,
      button: true,
      child: AnimatedContainer(
        duration: AppMotion.quick,
        curve: AppMotion.easeOut,
        decoration: BoxDecoration(
          color: selected
              ? tokens.amber.withValues(alpha: 0.08)
              : tokens.surface1,
          borderRadius: BorderRadius.circular(AppRadii.lg),
          border: Border.all(color: selected ? tokens.amber : tokens.hair),
        ),
        child: Material(
          type: MaterialType.transparency,
          borderRadius: BorderRadius.circular(AppRadii.lg),
          clipBehavior: Clip.antiAlias,
          child: InkWell(
            onTap: enabled
                ? () {
                    HapticFeedback.selectionClick();
                    onTap();
                  }
                : null,
            child: Padding(
              padding: const EdgeInsets.all(AppSpace.s4),
              child: Row(
                children: [
                  Icon(
                    icon,
                    color: selected ? tokens.amberText : tokens.textSecondary,
                  ),
                  const SizedBox(width: AppSpace.s3 + 2),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(title, style: textTheme.bodyLarge),
                        const SizedBox(height: 2),
                        Text(
                          subtitle,
                          style: textTheme.bodySmall?.copyWith(
                            color: tokens.textTertiary,
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(width: AppSpace.s3),
                  _Radio(selected: selected),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _Radio extends StatelessWidget {
  const _Radio({required this.selected});

  final bool selected;

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    return AnimatedContainer(
      duration: AppMotion.quick,
      width: 22,
      height: 22,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: selected ? tokens.amber : Colors.transparent,
        border: selected ? null : Border.all(color: tokens.line, width: 1.5),
      ),
      child: selected
          ? Icon(Icons.check_rounded, size: 16, color: tokens.onAmber)
          : null,
    );
  }
}
