import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/theme/tokens.dart';
import '../../../core/widgets/primary_button.dart';
import 'auth_providers.dart';
import 'widgets/otp_input.dart';
import 'widgets/resend_countdown.dart';

/// Design 1.3: six boxes, auto-submits on the 6th digit, resend after a cooldown.
class OtpScreen extends ConsumerStatefulWidget {
  const OtpScreen({super.key});

  @override
  ConsumerState<OtpScreen> createState() => _OtpScreenState();
}

class _OtpScreenState extends ConsumerState<OtpScreen> {
  final _code = TextEditingController();

  @override
  void dispose() {
    _code.dispose();
    super.dispose();
  }

  PhoneAuthController get _controller =>
      ref.read(phoneAuthControllerProvider.notifier);

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    final textTheme = Theme.of(context).textTheme;
    final state = ref.watch(phoneAuthControllerProvider);
    final verifying = state.step == PhoneAuthStep.verifying;
    final busy = verifying || state.step == PhoneAuthStep.sendingCode;

    // A wrong code clears the boxes so the next attempt starts fresh.
    ref.listen(phoneAuthControllerProvider.select((s) => s.error), (_, error) {
      if (error != null) _code.clear();
    });

    // Android system back = "change number", not "exit the app".
    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, _) {
        if (!didPop && !busy) _controller.editNumber();
      },
      child: Scaffold(
        appBar: AppBar(
          backgroundColor: Colors.transparent,
          leading: IconButton(
            tooltip: 'Change number',
            icon: const Icon(Icons.arrow_back),
            onPressed: _controller.editNumber,
          ),
        ),
        body: SafeArea(
          top: false,
          child: Padding(
            padding: const EdgeInsets.fromLTRB(
              AppSpace.s5,
              AppSpace.s3,
              AppSpace.s5,
              AppSpace.s5,
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Text('Enter the code', style: textTheme.displayMedium),
                const SizedBox(height: AppSpace.s2),
                Row(
                  children: [
                    Flexible(
                      child: Text(
                        'Sent to ${_formatPhone(state.phoneE164)}',
                        style: textTheme.bodyLarge?.copyWith(
                          color: tokens.textSecondary,
                        ),
                      ),
                    ),
                    TextButton(
                      onPressed: busy ? null : _controller.editNumber,
                      child: const Text('Edit'),
                    ),
                  ],
                ),
                const SizedBox(height: AppSpace.s5),
                OtpInput(
                  controller: _code,
                  enabled: !busy,
                  hasError: state.error != null,
                  onCompleted: _controller.verify,
                ),
                if (state.error != null) ...[
                  const SizedBox(height: AppSpace.s3),
                  Text(
                    state.error!,
                    style: textTheme.bodyMedium?.copyWith(
                      color: tokens.wineText,
                    ),
                  ),
                ],
                const SizedBox(height: AppSpace.s2),
                if (state.codeSentAt != null)
                  ResendCountdown(
                    sentAt: state.codeSentAt!,
                    enabled: !busy,
                    onResend: _controller.resend,
                  ),
                const Spacer(),
                ValueListenableBuilder<TextEditingValue>(
                  valueListenable: _code,
                  builder: (context, value, _) => PrimaryButton(
                    label: 'Verify',
                    loading: verifying,
                    onPressed:
                        value.text.length == PhoneAuthController.codeLength
                        ? () => _controller.verify(value.text)
                        : null,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  /// "+911234567890" → "+91 12345 67890"
  static String _formatPhone(String? e164) {
    if (e164 == null || e164.length != 13) return e164 ?? '';
    return '${e164.substring(0, 3)} ${e164.substring(3, 8)} ${e164.substring(8)}';
  }
}
