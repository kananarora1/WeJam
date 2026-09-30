import 'dart:ui' show ImageFilter, lerpDouble;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/theme/tokens.dart';
import 'auth_providers.dart';

/// Becomes true once the splash has played. The router keeps the user on the splash until then.
final splashCompletedProvider = NotifierProvider<SplashCompleted, bool>(
  SplashCompleted.new,
);

class SplashCompleted extends Notifier<bool> {
  @override
  bool build() => false;

  void complete() => state = true;
}

/// Cold-start splash: the wordmark focuses in from a blur, holds, waits for the session check,
/// then pops out toward the viewer before the first real screen fades in.
class SplashScreen extends ConsumerStatefulWidget {
  const SplashScreen({super.key});

  @override
  ConsumerState<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends ConsumerState<SplashScreen>
    with TickerProviderStateMixin {
  static const _focusIn = Duration(milliseconds: 700);
  static const _hold = Duration(milliseconds: 350);
  static const _popOut = Duration(milliseconds: 450);

  static const _startBlur = 16.0;
  static const _endBlur = 6.0;
  static const _startScale = 0.94;
  static const _endScale = 1.5;

  late final AnimationController _in = AnimationController(
    vsync: this,
    duration: _focusIn,
  );
  late final AnimationController _out = AnimationController(
    vsync: this,
    duration: _popOut,
  );
  bool _started = false;
  bool _reduceMotion = false;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    // MediaQuery isn't available in initState, so the sequence starts here, once.
    if (_started) return;
    _started = true;
    _reduceMotion = MediaQuery.disableAnimationsOf(context);
    if (_reduceMotion) {
      _in.duration = AppMotion.quick;
      _out.duration = AppMotion.quick;
    }
    _play();
  }

  Future<void> _play() async {
    await _in.forward();
    if (!_reduceMotion) await Future<void>.delayed(_hold);
    try {
      await ref.read(authStateProvider.future);
    } catch (_) {
      // A failed session check is handled by the router (treated as signed out).
    }
    if (!mounted) return;
    // Finish the pop-out before handing over: the router replaces this page instantly
    // (only the incoming page animates), so an early hand-off would cut the pop-out short.
    await _out.forward();
    if (!mounted) return;
    ref.read(splashCompletedProvider.notifier).complete();
  }

  @override
  void dispose() {
    _in.dispose();
    _out.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    final textTheme = Theme.of(context).textTheme;

    final wordmark = Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Text.rich(
          TextSpan(
            text: 'we',
            children: [
              TextSpan(
                text: 'jam',
                style: TextStyle(
                  color: tokens.amberText,
                  fontStyle: FontStyle.italic,
                ),
              ),
            ],
          ),
          style: textTheme.displayLarge?.copyWith(fontSize: 72, height: 1),
        ),
        const SizedBox(height: AppSpace.s3),
        Text(
          'Live music, the room next door.',
          style: textTheme.bodyLarge?.copyWith(color: tokens.textSecondary),
        ),
      ],
    );

    return Scaffold(
      body: AnimatedBuilder(
        animation: Listenable.merge([_in, _out]),
        child: wordmark,
        builder: (context, child) {
          final focus = AppMotion.easeOut.transform(_in.value);
          // Pop-out: grow early (toward the viewer), fade late, so it reads as a zoom, not a smear.
          final grow = Curves.easeOutCubic.transform(_out.value);
          final fade = Curves.easeInQuad.transform(_out.value);
          final visibility = focus * (1 - fade);
          final blur = _reduceMotion
              ? 0.0
              : _startBlur * (1 - focus) + _endBlur * fade;
          final scale = _reduceMotion
              ? 1.0
              : lerpDouble(_startScale, 1, focus)! *
                    lerpDouble(1, _endScale, grow)!;

          return Stack(
            fit: StackFit.expand,
            children: [
              // The "stage light": one soft amber pool from above, lit with the wordmark.
              Opacity(
                opacity: visibility,
                child: DecoratedBox(
                  decoration: BoxDecoration(
                    gradient: RadialGradient(
                      center: const Alignment(0, -1.1),
                      radius: 1.1,
                      colors: [
                        tokens.amber.withValues(alpha: 0.16),
                        tokens.bg.withValues(alpha: 0),
                      ],
                    ),
                  ),
                ),
              ),
              Center(
                child: Transform.scale(
                  scale: scale,
                  child: Opacity(
                    opacity: visibility,
                    child: blur < 0.1
                        ? child
                        : ImageFiltered(
                            imageFilter: ImageFilter.blur(
                              sigmaX: blur,
                              sigmaY: blur,
                            ),
                            child: child,
                          ),
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}
