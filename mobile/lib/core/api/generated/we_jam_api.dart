// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:dio/dio.dart';

import 'clients/auth_client.dart';
import 'clients/me_client.dart';
import 'clients/venues_client.dart';

/// WeJam API `vv1`
class WeJamApi {
  WeJamApi(
    Dio dio, {
    String? baseUrl,
  })  : _dio = dio,
        _baseUrl = baseUrl;

  final Dio _dio;
  final String? _baseUrl;

  static String get version => 'v1';

  AuthClient? _auth;
  MeClient? _me;
  VenuesClient? _venues;

  AuthClient get auth => _auth ??= AuthClient(_dio, baseUrl: _baseUrl);

  MeClient get me => _me ??= MeClient(_dio, baseUrl: _baseUrl);

  VenuesClient get venues => _venues ??= VenuesClient(_dio, baseUrl: _baseUrl);
}
