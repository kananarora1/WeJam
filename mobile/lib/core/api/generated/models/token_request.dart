// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

part 'token_request.g.dart';

@JsonSerializable()
class TokenRequest {
  const TokenRequest({
    required this.firebaseIdToken,
  });
  
  factory TokenRequest.fromJson(Map<String, Object?> json) => _$TokenRequestFromJson(json);
  
  final String firebaseIdToken;

  Map<String, Object?> toJson() => _$TokenRequestToJson(this);
}
