import 'package:flutter/material.dart';

import 'tokens.dart';

ThemeData buildDarkTheme() {
  const t = AppTokens.dark;
  final textTheme = _textTheme(t);

  return ThemeData(
    useMaterial3: true,
    brightness: Brightness.dark,
    fontFamily: AppFonts.sans,
    scaffoldBackgroundColor: t.bg,
    colorScheme: ColorScheme.dark(
      primary: t.amber,
      onPrimary: t.onAmber,
      secondary: t.amberText,
      onSecondary: t.onAmber,
      surface: t.bg,
      onSurface: t.textPrimary,
      onSurfaceVariant: t.textSecondary,
      surfaceContainerHighest: t.surface2,
      outline: t.line,
      outlineVariant: t.hair,
      error: t.wineText,
      onError: t.onAmber,
    ),
    textTheme: textTheme,
    inputDecorationTheme: InputDecorationTheme(
      filled: true,
      fillColor: t.surface2,
      hintStyle: textTheme.bodyLarge?.copyWith(color: t.textTertiary),
      contentPadding: const EdgeInsets.symmetric(
        horizontal: AppSpace.s4,
        vertical: AppSpace.s4,
      ),
      border: _inputBorder(t.line),
      enabledBorder: _inputBorder(t.line),
      disabledBorder: _inputBorder(t.hair),
      focusedBorder: _inputBorder(t.amber, width: 1.5),
      errorBorder: _inputBorder(t.wine),
      focusedErrorBorder: _inputBorder(t.wineText, width: 1.5),
    ),
    filledButtonTheme: FilledButtonThemeData(
      style: FilledButton.styleFrom(
        backgroundColor: t.amber,
        foregroundColor: t.onAmber,
        disabledBackgroundColor: t.surface3,
        disabledForegroundColor: t.textTertiary,
        minimumSize: const Size.fromHeight(52),
        shape: const StadiumBorder(),
        textStyle: textTheme.labelLarge,
      ),
    ),
    textButtonTheme: TextButtonThemeData(
      style: TextButton.styleFrom(
        foregroundColor: t.amberText,
        textStyle: textTheme.labelMedium,
        minimumSize: const Size(44, 44),
      ),
    ),
    outlinedButtonTheme: OutlinedButtonThemeData(
      style: OutlinedButton.styleFrom(
        foregroundColor: t.textPrimary,
        side: BorderSide(color: t.line),
        minimumSize: const Size.fromHeight(52),
        shape: const StadiumBorder(),
        textStyle: textTheme.labelLarge,
      ),
    ),
    textSelectionTheme: TextSelectionThemeData(cursorColor: t.amber),
    extensions: const [AppTokens.dark],
  );
}

OutlineInputBorder _inputBorder(Color color, {double width = 1}) =>
    OutlineInputBorder(
      borderRadius: BorderRadius.circular(AppRadii.md),
      borderSide: BorderSide(color: color, width: width),
    );

/// Type scale from Foundations: Instrument Serif for display/titles, Jost for everything tappable.
TextTheme _textTheme(AppTokens t) {
  TextStyle serif(double size, double lineHeight) => TextStyle(
    fontFamily: AppFonts.serif,
    fontSize: size,
    height: lineHeight / size,
    color: t.textPrimary,
  );
  TextStyle sans(
    double size,
    double lineHeight,
    FontWeight weight, {
    double tracking = 0,
  }) => TextStyle(
    fontFamily: AppFonts.sans,
    fontSize: size,
    height: lineHeight / size,
    fontWeight: weight,
    letterSpacing: size * tracking,
    color: t.textPrimary,
  );

  return TextTheme(
    displayLarge: serif(48, 52), // display.xl: celebrations, splash
    displayMedium: serif(34, 38), // display.l: screen titles
    headlineLarge: serif(28, 32), // title: event name on detail
    headlineMedium: serif(22, 26), // headline: event name on cards
    bodyLarge: sans(17, 24, FontWeight.w400), // body.l
    bodyMedium: sans(15, 22, FontWeight.w400), // body
    labelLarge: sans(16, 20, FontWeight.w600, tracking: 0.01), // button
    labelMedium: sans(13, 16, FontWeight.w500, tracking: 0.02), // label
    labelSmall: sans(
      11,
      14,
      FontWeight.w500,
      tracking: 0.14,
    ), // overline (uppercase the text itself)
  );
}
