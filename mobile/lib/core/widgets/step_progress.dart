import 'package:flutter/material.dart';

import '../theme/tokens.dart';

/// Thin segmented bar for multi-step flows: done and current steps lit amber.
class StepProgress extends StatelessWidget {
  const StepProgress({super.key, required this.step, required this.of});

  /// 1-based.
  final int step;
  final int of;

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    return Semantics(
      label: 'Step $step of $of',
      child: Row(
        children: [
          for (var i = 1; i <= of; i++) ...[
            if (i > 1) const SizedBox(width: AppSpace.s1),
            Expanded(
              child: Container(
                height: 3,
                decoration: BoxDecoration(
                  color: i <= step ? tokens.amber : tokens.surface3,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }
}
