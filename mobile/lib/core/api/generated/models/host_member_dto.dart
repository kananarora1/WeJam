// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

part 'host_member_dto.g.dart';

/// Owner first (admin), then accepted members. Pending invites are never listed here.
@JsonSerializable()
class HostMemberDto {
  const HostMemberDto({
    required this.admin,
    required this.displayName,
    required this.userId,
  });
  
  factory HostMemberDto.fromJson(Map<String, Object?> json) => _$HostMemberDtoFromJson(json);
  
  /// The profile owner
  final bool admin;
  final String? displayName;
  final String userId;

  Map<String, Object?> toJson() => _$HostMemberDtoToJson(this);
}
