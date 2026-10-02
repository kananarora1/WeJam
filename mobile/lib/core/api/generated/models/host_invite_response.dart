// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'host_invite_response_group_kind.dart';

part 'host_invite_response.g.dart';

@JsonSerializable()
class HostInviteResponse {
  const HostInviteResponse({
    required this.groupKind,
    required this.groupName,
    required this.hostProfileId,
    required this.invitedAt,
    required this.invitedByName,
  });
  
  factory HostInviteResponse.fromJson(Map<String, Object?> json) => _$HostInviteResponseFromJson(json);
  
  final HostInviteResponseGroupKind groupKind;
  final String groupName;
  final String hostProfileId;
  final DateTime invitedAt;
  final String? invitedByName;

  Map<String, Object?> toJson() => _$HostInviteResponseToJson(this);
}
