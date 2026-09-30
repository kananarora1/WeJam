import 'package:dio/dio.dart';

/// An API failure with copy that is safe to show the user as-is.
class ApiFailure implements Exception {
  const ApiFailure(this.message, {this.statusCode});

  final String message;
  final int? statusCode;

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
    if (status != null && status < 500 && detail is String) {
      return ApiFailure(detail, statusCode: status);
    }
    return ApiFailure(
      "Something went wrong on our side. It's us, not you — try again in a moment.",
      statusCode: status,
    );
  }
}
