import 'dart:math' as math;

import 'package:flutter/material.dart';

/// Loading indicator from the design system ("loading = EQ bars, never a spinner").
/// Freezes on a static frame when the OS asks for reduced motion.
class EqBars extends StatefulWidget {
  const EqBars({super.key, required this.color, this.height = 18});

  final Color color;
  final double height;

  @override
  State<EqBars> createState() => _EqBarsState();
}

class _EqBarsState extends State<EqBars> with SingleTickerProviderStateMixin {
  static const _bars = 4;
  late final AnimationController _controller = AnimationController(
    vsync: this,
    duration: const Duration(milliseconds: 900),
  );

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (MediaQuery.disableAnimationsOf(context)) {
      _controller.stop();
    } else if (!_controller.isAnimating) {
      _controller.repeat();
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Semantics(
      label: 'Loading',
      child: SizedBox(
        height: widget.height,
        child: AnimatedBuilder(
          animation: _controller,
          builder: (context, _) => Row(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              for (var i = 0; i < _bars; i++)
                Container(
                  width: 3,
                  height: widget.height * _barFraction(i),
                  margin: const EdgeInsets.symmetric(horizontal: 1.5),
                  decoration: BoxDecoration(
                    color: widget.color,
                    borderRadius: BorderRadius.circular(1.5),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }

  double _barFraction(int index) {
    final phase = (_controller.value + index * 0.23) * 2 * math.pi;
    return 0.3 + 0.7 * (0.5 + 0.5 * math.sin(phase));
  }
}
