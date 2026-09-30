import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/theme/tokens.dart';
import '../../../core/widgets/primary_button.dart';
import 'auth_providers.dart';

/// Design 1.2: single field, numeric keypad, +91.
class PhoneScreen extends ConsumerStatefulWidget {
  const PhoneScreen({super.key});

  @override
  ConsumerState<PhoneScreen> createState() => _PhoneScreenState();
}

class _PhoneScreenState extends ConsumerState<PhoneScreen> {
  late final TextEditingController _number;

  @override
  void initState() {
    super.initState();
    final previous = ref.read(phoneAuthControllerProvider).phoneE164;
    _number = TextEditingController(
      text: previous?.substring(PhoneAuthController.countryCode.length) ?? '',
    );
  }

  @override
  void dispose() {
    _number.dispose();
    super.dispose();
  }

  void _submit() =>
      ref.read(phoneAuthControllerProvider.notifier).sendCode(_number.text);

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    final textTheme = Theme.of(context).textTheme;
    final step = ref.watch(phoneAuthControllerProvider.select((s) => s.step));
    final error = ref.watch(phoneAuthControllerProvider.select((s) => s.error));
    final sending = step == PhoneAuthStep.sendingCode;

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
              Text("What's your number?", style: textTheme.displayMedium),
              const SizedBox(height: AppSpace.s3),
              Text(
                "We'll text you a 6-digit code. No passwords, ever.",
                style: textTheme.bodyLarge?.copyWith(
                  color: tokens.textSecondary,
                ),
              ),
              const SizedBox(height: AppSpace.s6),
              Text(
                'Mobile number',
                style: textTheme.labelMedium?.copyWith(
                  color: tokens.textSecondary,
                ),
              ),
              const SizedBox(height: AppSpace.s2),
              TextField(
                controller: _number,
                enabled: !sending,
                autofocus: true,
                keyboardType: TextInputType.phone,
                autofillHints: const [AutofillHints.telephoneNumberNational],
                inputFormatters: [
                  FilteringTextInputFormatter.digitsOnly,
                  LengthLimitingTextInputFormatter(
                    PhoneAuthController.localNumberLength,
                  ),
                ],
                style: textTheme.bodyLarge?.copyWith(
                  fontFeatures: const [FontFeature.tabularFigures()],
                ),
                decoration: InputDecoration(
                  hintText: '98450 12345',
                  prefixIcon: Padding(
                    padding: const EdgeInsets.only(
                      left: AppSpace.s4,
                      right: AppSpace.s2,
                    ),
                    child: Text(
                      'IN ${PhoneAuthController.countryCode}',
                      style: textTheme.bodyLarge?.copyWith(
                        color: tokens.textSecondary,
                      ),
                    ),
                  ),
                  prefixIconConstraints: const BoxConstraints(
                    minWidth: 0,
                    minHeight: 0,
                  ),
                ),
                onSubmitted: (_) => _submit(),
              ),
              if (error != null) ...[
                const SizedBox(height: AppSpace.s2),
                Text(
                  error,
                  style: textTheme.bodyMedium?.copyWith(color: tokens.wineText),
                ),
              ],
              const Spacer(),
              ValueListenableBuilder<TextEditingValue>(
                valueListenable: _number,
                builder: (context, value, _) => PrimaryButton(
                  label: 'Send code',
                  loading: sending,
                  onPressed: PhoneAuthController.isValidLocalNumber(value.text)
                      ? _submit
                      : null,
                ),
              ),
              const SizedBox(height: AppSpace.s3),
              Text(
                'By continuing you agree to the Terms and Privacy Policy.',
                textAlign: TextAlign.center,
                style: textTheme.labelMedium?.copyWith(
                  color: tokens.textTertiary,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
