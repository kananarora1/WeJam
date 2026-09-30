import 'dart:async';

import 'package:flutter/material.dart';

import '../../../../core/theme/tokens.dart';

/// "Resend in 0:24" until [cooldown] has passed since [sentAt], then a "Resend code" button.
class ResendCountdown extends StatefulWidget {
  const ResendCountdown({
    super.key,
    required this.sentAt,
    required this.onResend,
    this.cooldown = const Duration(seconds: 30),
    this.enabled = true,
  });

  final DateTime sentAt;
  final VoidCallback onResend;
  final Duration cooldown;
  final bool enabled;

  @override
  State<ResendCountdown> createState() => _ResendCountdownState();
}

class _ResendCountdownState extends State<ResendCountdown> {
  Timer? _ticker;

  @override
  void initState() {
    super.initState();
    _startTicking();
  }

  @override
  void didUpdateWidget(ResendCountdown oldWidget) {
    super.didUpdateWidget(oldWidget);
    // A resend restarts the cooldown.
    if (oldWidget.sentAt != widget.sentAt) _startTicking();
  }

  void _startTicking() {
    _ticker?.cancel();
    _ticker = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_remaining == Duration.zero) timer.cancel();
      setState(() {});
    });
  }

  Duration get _remaining {
    final left = widget.cooldown - DateTime.now().difference(widget.sentAt);
    return left.isNegative ? Duration.zero : left;
  }

  @override
  void dispose() {
    _ticker?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    final textTheme = Theme.of(context).textTheme;
    final remaining = _remaining;

    if (remaining == Duration.zero) {
      return Align(
        alignment: Alignment.centerLeft,
        child: TextButton(
          onPressed: widget.enabled ? widget.onResend : null,
          child: const Text('Resend code'),
        ),
      );
    }

    final seconds = remaining.inSeconds.toString().padLeft(2, '0');
    return SizedBox(
      height: 44,
      child: Align(
        alignment: Alignment.centerLeft,
        child: Text(
          "Didn't get it? Resend in 0:$seconds",
          style: textTheme.bodyMedium?.copyWith(
            color: tokens.textTertiary,
            fontFeatures: const [FontFeature.tabularFigures()],
          ),
        ),
      ),
    );
  }
}
