import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../../core/theme/tokens.dart';

/// Six boxes backed by one invisible TextField, so paste and SMS autofill ("oneTimeCode") work natively.
/// Calls [onCompleted] as soon as the last digit is entered.
class OtpInput extends StatefulWidget {
  const OtpInput({
    super.key,
    required this.controller,
    required this.onCompleted,
    this.length = 6,
    this.enabled = true,
    this.hasError = false,
  });

  final TextEditingController controller;
  final ValueChanged<String> onCompleted;
  final int length;
  final bool enabled;
  final bool hasError;

  @override
  State<OtpInput> createState() => _OtpInputState();
}

class _OtpInputState extends State<OtpInput> {
  final _focusNode = FocusNode();

  @override
  void initState() {
    super.initState();
    _focusNode.addListener(() => setState(() {}));
  }

  @override
  void dispose() {
    _focusNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        ValueListenableBuilder<TextEditingValue>(
          valueListenable: widget.controller,
          builder: (context, value, _) => Row(
            children: [
              for (var i = 0; i < widget.length; i++) ...[
                if (i > 0) const SizedBox(width: AppSpace.s2),
                Expanded(child: _box(context, value.text, i)),
              ],
            ],
          ),
        ),
        Positioned.fill(
          child: Opacity(
            opacity: 0,
            child: TextField(
              key: const Key('otp-field'),
              controller: widget.controller,
              focusNode: _focusNode,
              enabled: widget.enabled,
              autofocus: true,
              keyboardType: TextInputType.number,
              autofillHints: const [AutofillHints.oneTimeCode],
              inputFormatters: [
                FilteringTextInputFormatter.digitsOnly,
                LengthLimitingTextInputFormatter(widget.length),
              ],
              showCursor: false,
              enableInteractiveSelection: false,
              decoration: const InputDecoration(
                border: InputBorder.none,
                filled: false,
                counterText: '',
              ),
              onChanged: (code) {
                if (code.length == widget.length) widget.onCompleted(code);
              },
            ),
          ),
        ),
      ],
    );
  }

  Widget _box(BuildContext context, String code, int index) {
    final tokens = context.tokens;
    final digit = index < code.length ? code[index] : '';
    final isActive =
        _focusNode.hasFocus &&
        widget.enabled &&
        index == code.length.clamp(0, widget.length - 1);
    final borderColor = widget.hasError
        ? tokens.wine
        : isActive
        ? tokens.amber
        : tokens.line;

    return AnimatedContainer(
      duration: AppMotion.quick,
      curve: AppMotion.easeOut,
      height: 56,
      alignment: Alignment.center,
      decoration: BoxDecoration(
        color: tokens.surface2,
        borderRadius: BorderRadius.circular(AppRadii.sm),
        border: Border.all(color: borderColor, width: isActive ? 1.5 : 1),
      ),
      child: Text(
        digit,
        style: Theme.of(context).textTheme.headlineMedium?.copyWith(
          fontFamily: AppFonts.sans,
          fontFeatures: const [FontFeature.tabularFigures()],
        ),
      ),
    );
  }
}
