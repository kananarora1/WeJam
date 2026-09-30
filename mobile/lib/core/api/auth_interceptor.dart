import 'package:dio/dio.dart';

import 'app_token_store.dart';

/// Attaches the app JWT to every request. On a 401 it asks for a fresh JWT once and retries the
/// request once; a second 401 (or a failed re-authentication) is passed on to the caller.
class AuthInterceptor extends Interceptor {
  AuthInterceptor({
    required this._dio,
    required this._tokens,
    required this._reauthenticate,
  });

  static const tokenExchangePath = '/api/v1/auth/token';
  static const _retriedKey = 'auth.retried';

  final Dio _dio;
  final AppTokenStore _tokens;
  final Future<bool> Function() _reauthenticate;

  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    final token = _tokens.accessToken;
    if (token != null && options.path != tokenExchangePath) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    final request = err.requestOptions;
    final shouldRetry =
        err.response?.statusCode == 401 &&
        request.path != tokenExchangePath &&
        request.extra[_retriedKey] != true;
    if (!shouldRetry || !await _reauthenticate()) {
      handler.next(err);
      return;
    }
    try {
      request.extra[_retriedKey] = true;
      handler.resolve(await _dio.fetch<Object?>(request));
    } on DioException catch (retryError) {
      handler.next(retryError);
    }
  }
}
