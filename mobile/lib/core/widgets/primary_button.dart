import 'package:flutter/material.dart';

import '../theme/tokens.dart';
import 'eq_bars.dart';

/// The one amber action per screen. While [loading], it stays amber and shows EQ bars instead of the label.
class PrimaryButton extends StatelessWidget {
  const PrimaryButton({
    super.key,
    required this.label,
    required this.onPressed,
    this.loading = false,
  });

  final String label;
  final VoidCallback? onPressed;
  final bool loading;

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    return FilledButton(
      onPressed: loading ? null : onPressed,
      style: loading
          ? FilledButton.styleFrom(
              disabledBackgroundColor: tokens.amber,
              disabledForegroundColor: tokens.onAmber,
            )
          : null,
      child: loading ? EqBars(color: tokens.onAmber) : Text(label),
    );
  }
}
