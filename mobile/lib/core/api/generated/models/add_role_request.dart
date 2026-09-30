// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'add_role_request_role.dart';

part 'add_role_request.g.dart';

@JsonSerializable()
class AddRoleRequest {
  const AddRoleRequest({
    required this.role,
  });
  
  factory AddRoleRequest.fromJson(Map<String, Object?> json) => _$AddRoleRequestFromJson(json);
  
  /// Only HOST or VENUE_ADMIN are accepted
  final AddRoleRequestRole role;

  Map<String, Object?> toJson() => _$AddRoleRequestToJson(this);
}
