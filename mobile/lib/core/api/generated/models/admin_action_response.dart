// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'admin_action_response_action.dart';
import 'admin_action_response_target_type.dart';

part 'admin_action_response.g.dart';

@JsonSerializable()
class AdminActionResponse {
  const AdminActionResponse({
    required this.action,
    required this.adminId,
    required this.adminName,
    required this.createdAt,
    required this.id,
    required this.reason,
    required this.targetId,
    required this.targetType,
  });
  
  factory AdminActionResponse.fromJson(Map<String, Object?> json) => _$AdminActionResponseFromJson(json);
  
  final AdminActionResponseAction action;
  final String adminId;
  final String? adminName;
  final DateTime createdAt;
  final String id;
  final String? reason;
  final String targetId;
  final AdminActionResponseTargetType targetType;

  Map<String, Object?> toJson() => _$AdminActionResponseToJson(this);
}
