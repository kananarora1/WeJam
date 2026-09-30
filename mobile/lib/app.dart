import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'core/router/router.dart';
import 'core/theme/app_theme.dart';

class WeJamApp extends ConsumerWidget {
  const WeJamApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return MaterialApp.router(
      title: 'WeJam',
      debugShowCheckedModeBanner: false,
      theme: buildDarkTheme(),
      routerConfig: ref.watch(routerProvider),
    );
  }
}
