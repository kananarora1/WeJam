// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

part 'token_response.g.dart';

@JsonSerializable()
class TokenResponse {
  const TokenResponse({
    required this.accessToken,
    required this.expiresIn,
    required this.tokenType,
  });
  
  factory TokenResponse.fromJson(Map<String, Object?> json) => _$TokenResponseFromJson(json);
  
  final String accessToken;

  /// Seconds until accessToken expires
  final int expiresIn;
  final String tokenType;

  Map<String, Object?> toJson() => _$TokenResponseToJson(this);
}
