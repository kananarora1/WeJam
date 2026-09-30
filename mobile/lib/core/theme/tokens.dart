import 'package:flutter/material.dart';

/// Theme-dependent colors from the design's Foundations board.
/// Registered on [ThemeData.extensions]; read with `context.tokens`.
@immutable
class AppTokens extends ThemeExtension<AppTokens> {
  const AppTokens({
    required this.bg,
    required this.surface1,
    required this.surface2,
    required this.surface3,
    required this.line,
    required this.hair,
    required this.textPrimary,
    required this.textSecondary,
    required this.textTertiary,
    required this.amber,
    required this.onAmber,
    required this.amberText,
    required this.amberTint,
    required this.wine,
    required this.wineText,
  });

  final Color bg;
  final Color surface1;
  final Color surface2;
  final Color surface3;
  final Color line;
  final Color hair;
  final Color textPrimary;
  final Color textSecondary;
  final Color textTertiary;

  /// The single "stage light" accent: fills only. Use [amberText] for amber text.
  final Color amber;
  final Color onAmber;
  final Color amberText;
  final Color amberTint;

  /// Decline / destructive. Never a red flash.
  final Color wine;
  final Color wineText;

  static const dark = AppTokens(
    bg: Color(0xFF141110),
    surface1: Color(0xFF1C1816),
    surface2: Color(0xFF241E1A),
    surface3: Color(0xFF2E2621),
    line: Color(0xFF3A3029),
    hair: Color(0xFF2A231F),
    textPrimary: Color(0xFFF3EBDD),
    textSecondary: Color(0xFFB9AD9B),
    textTertiary: Color(0xFF8C8172),
    amber: Color(0xFFE8A94A),
    onAmber: Color(0xFF1A1208),
    amberText: Color(0xFFEDB55E),
    amberTint: Color(0x24E8A94A),
    wine: Color(0xFF8E3B46),
    wineText: Color(0xFFE28C96),
  );

  @override
  AppTokens copyWith({
    Color? bg,
    Color? surface1,
    Color? surface2,
    Color? surface3,
    Color? line,
    Color? hair,
    Color? textPrimary,
    Color? textSecondary,
    Color? textTertiary,
    Color? amber,
    Color? onAmber,
    Color? amberText,
    Color? amberTint,
    Color? wine,
    Color? wineText,
  }) {
    return AppTokens(
      bg: bg ?? this.bg,
      surface1: surface1 ?? this.surface1,
      surface2: surface2 ?? this.surface2,
      surface3: surface3 ?? this.surface3,
      line: line ?? this.line,
      hair: hair ?? this.hair,
      textPrimary: textPrimary ?? this.textPrimary,
      textSecondary: textSecondary ?? this.textSecondary,
      textTertiary: textTertiary ?? this.textTertiary,
      amber: amber ?? this.amber,
      onAmber: onAmber ?? this.onAmber,
      amberText: amberText ?? this.amberText,
      amberTint: amberTint ?? this.amberTint,
      wine: wine ?? this.wine,
      wineText: wineText ?? this.wineText,
    );
  }

  @override
  AppTokens lerp(AppTokens? other, double t) {
    if (other == null) return this;
    return AppTokens(
      bg: Color.lerp(bg, other.bg, t)!,
      surface1: Color.lerp(surface1, other.surface1, t)!,
      surface2: Color.lerp(surface2, other.surface2, t)!,
      surface3: Color.lerp(surface3, other.surface3, t)!,
      line: Color.lerp(line, other.line, t)!,
      hair: Color.lerp(hair, other.hair, t)!,
      textPrimary: Color.lerp(textPrimary, other.textPrimary, t)!,
      textSecondary: Color.lerp(textSecondary, other.textSecondary, t)!,
      textTertiary: Color.lerp(textTertiary, other.textTertiary, t)!,
      amber: Color.lerp(amber, other.amber, t)!,
      onAmber: Color.lerp(onAmber, other.onAmber, t)!,
      amberText: Color.lerp(amberText, other.amberText, t)!,
      amberTint: Color.lerp(amberTint, other.amberTint, t)!,
      wine: Color.lerp(wine, other.wine, t)!,
      wineText: Color.lerp(wineText, other.wineText, t)!,
    );
  }
}

/// Theme-independent tokens: identical in dark and light, so plain constants.
abstract final class AppSpace {
  static const double s1 = 4;
  static const double s2 = 8;
  static const double s3 = 12;
  static const double s4 = 16; // card padding
  static const double s5 = 20; // screen gutter
  static const double s6 = 28; // between sections
  static const double s7 = 40;
  static const double s8 = 56;
}

abstract final class AppRadii {
  static const double sm = 10; // thumbs, OTP boxes
  static const double md = 14; // inputs, tiles
  static const double lg = 18; // cards
  static const double xl = 28; // sheet tops
  static const double pill = 999;
}

abstract final class AppMotion {
  static const quick = Duration(milliseconds: 200);
  static const standard = Duration(milliseconds: 280);
  static const slow = Duration(milliseconds: 350);
  static const easeOut = Cubic(0.22, 1, 0.36, 1);
  static const easeInOut = Cubic(0.65, 0, 0.35, 1);
}

abstract final class AppFonts {
  static const serif = 'InstrumentSerif';
  static const sans = 'Jost';
}

extension AppTokensContext on BuildContext {
  AppTokens get tokens => Theme.of(this).extension<AppTokens>()!;
}
