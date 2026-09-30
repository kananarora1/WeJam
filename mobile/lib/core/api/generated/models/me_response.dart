// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'me_response_roles.dart';

part 'me_response.g.dart';

@JsonSerializable()
class MeResponse {
  const MeResponse({
    required this.displayName,
    required this.id,
    required this.phone,
    required this.roles,
  });
  
  factory MeResponse.fromJson(Map<String, Object?> json) => _$MeResponseFromJson(json);
  
  /// Null until the user completes their profile
  final String? displayName;
  final String id;

  /// E.164; null for non-phone sign-in methods
  final String? phone;
  final List<MeResponseRoles> roles;

  Map<String, Object?> toJson() => _$MeResponseToJson(this);
}
