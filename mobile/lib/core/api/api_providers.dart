import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../features/account/presentation/session_controller.dart';
import 'api_config.dart';
import 'app_token_store.dart';
import 'auth_interceptor.dart';
import 'generated/we_jam_api.dart';

final dioProvider = Provider<Dio>((ref) {
  final dio = Dio(
    BaseOptions(
      baseUrl: ApiConfig.baseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 15),
    ),
  );
  dio.interceptors.add(
    AuthInterceptor(
      dio: dio,
      tokens: ref.read(appTokenStoreProvider),
      // Read lazily at call time: the session itself depends on this Dio.
      reauthenticate: () =>
          ref.read(sessionControllerProvider.notifier).reauthenticate(),
    ),
  );
  ref.onDispose(dio.close);
  return dio;
});

final apiProvider = Provider<WeJamApi>(
  (ref) => WeJamApi(ref.watch(dioProvider)),
);
