import 'dart:typed_data';

import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wejam/core/api/app_token_store.dart';
import 'package:wejam/core/api/auth_interceptor.dart';

/// Answers each request with the next scripted status code and records what was sent.
class ScriptedAdapter implements HttpClientAdapter {
  ScriptedAdapter(this.statuses);

  final List<int> statuses;
  final sent = <RequestOptions>[];

  @override
  Future<ResponseBody> fetch(
    RequestOptions options,
    Stream<Uint8List>? requestStream,
    Future<void>? cancelFuture,
  ) async {
    sent.add(options);
    return ResponseBody.fromString(
      '{}',
      statuses[sent.length - 1],
      headers: {
        Headers.contentTypeHeader: [Headers.jsonContentType],
      },
    );
  }

  @override
  void close({bool force = false}) {}
}

void main() {
  late AppTokenStore tokens;
  late int reauthCalls;
  late bool reauthSucceeds;

  Dio dioWith(ScriptedAdapter adapter) {
    final dio = Dio(BaseOptions(baseUrl: 'http://test'))
      ..httpClientAdapter = adapter;
    dio.interceptors.add(
      AuthInterceptor(
        dio: dio,
        tokens: tokens,
        reauthenticate: () async {
          reauthCalls++;
          if (reauthSucceeds) tokens.accessToken = 'new-jwt';
          return reauthSucceeds;
        },
      ),
    );
    return dio;
  }

  setUp(() {
    tokens = AppTokenStore()..accessToken = 'old-jwt';
    reauthCalls = 0;
    reauthSucceeds = true;
  });

  String? authHeader(RequestOptions options) =>
      options.headers['Authorization'] as String?;

  test('attaches the app JWT, except on the token exchange itself', () async {
    final adapter = ScriptedAdapter([200, 200]);
    final dio = dioWith(adapter);

    await dio.get<Object?>('/api/v1/me');
    await dio.post<Object?>(AuthInterceptor.tokenExchangePath);

    expect(authHeader(adapter.sent[0]), 'Bearer old-jwt');
    expect(authHeader(adapter.sent[1]), isNull);
  });

  test('a 401 re-authenticates once and retries with the new JWT', () async {
    final adapter = ScriptedAdapter([401, 200]);

    final response = await dioWith(adapter).get<Object?>('/api/v1/me');

    expect(response.statusCode, 200);
    expect(reauthCalls, 1);
    expect(adapter.sent, hasLength(2));
    expect(authHeader(adapter.sent[1]), 'Bearer new-jwt');
  });

  test('a second 401 is returned to the caller, not retried again', () async {
    final adapter = ScriptedAdapter([401, 401]);

    await expectLater(
      dioWith(adapter).get<Object?>('/api/v1/me'),
      throwsA(
        isA<DioException>().having(
          (e) => e.response?.statusCode,
          'status',
          401,
        ),
      ),
    );
    expect(reauthCalls, 1);
    expect(adapter.sent, hasLength(2));
  });

  test(
    'if re-authentication fails the original 401 is returned without a retry',
    () async {
      reauthSucceeds = false;
      final adapter = ScriptedAdapter([401]);

      await expectLater(
        dioWith(adapter).get<Object?>('/api/v1/me'),
        throwsA(isA<DioException>()),
      );
      expect(reauthCalls, 1);
      expect(adapter.sent, hasLength(1));
    },
  );

  test(
    'a 401 from the token exchange never triggers re-authentication',
    () async {
      final adapter = ScriptedAdapter([401]);

      await expectLater(
        dioWith(adapter).post<Object?>(AuthInterceptor.tokenExchangePath),
        throwsA(isA<DioException>()),
      );
      expect(reauthCalls, 0);
    },
  );
}
