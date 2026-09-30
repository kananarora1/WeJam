import 'package:flutter/material.dart';

import '../theme/tokens.dart';
import 'eq_bars.dart';

class LoadingScreen extends StatelessWidget {
  const LoadingScreen({super.key, required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            EqBars(color: tokens.amber, height: 28),
            const SizedBox(height: AppSpace.s4),
            Text(
              message,
              style: Theme.of(context).textTheme.bodyLarge
                  ?.copyWith(color: tokens.textSecondary),
            ),
          ],
        ),
      ),
    );
  }
}
