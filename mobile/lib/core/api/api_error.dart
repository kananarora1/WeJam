import 'package:dio/dio.dart';

/// An API failure with copy that is safe to show the user as-is.
class ApiFailure implements Exception {
  const ApiFailure(
    this.message, {
    this.statusCode,
    this.fieldErrors = const {},
  });

  final String message;
  final int? statusCode;

  /// Per-field problems from a 400 (`errors` in the problem detail), keyed by request field name.
  final Map<String, String> fieldErrors;

  bool get isUnauthorized => statusCode == 401;

  @override
  String toString() => 'ApiFailure($statusCode): $message';

  factory ApiFailure.fromDio(DioException e) {
    final status = e.response?.statusCode;
    switch (e.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
      case DioExceptionType.connectionError:
        return const ApiFailure(
          "Can't reach WeJam right now. Check your connection and try again.",
        );
      default:
        break;
    }
    if (status == 401) {
      return const ApiFailure(
        'Your session has ended. Sign in again.',
        statusCode: 401,
      );
    }
    // Backend errors are RFC 9457 problem details; 4xx `detail` is written for users.
    final data = e.response?.data;
    final detail = data is Map ? data['detail'] : null;
    final errors = data is Map ? data['errors'] : null;
    if (status != null && status < 500 && detail is String) {
      return ApiFailure(
        detail,
        statusCode: status,
        fieldErrors: {
          if (errors is Map)
            for (final entry in errors.entries)
              if (entry.value is String) '${entry.key}': entry.value as String,
        },
      );
    }
    return ApiFailure(
      "Something went wrong on our side. It's us, not you — try again in a moment.",
      statusCode: status,
    );
  }
}
