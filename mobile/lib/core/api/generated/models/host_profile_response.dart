// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'host_member_dto.dart';
import 'host_profile_response_genres.dart';
import 'host_profile_response_group_kind.dart';
import 'host_profile_response_type.dart';
import 'media_link_dto.dart';

part 'host_profile_response.g.dart';

@JsonSerializable()
class HostProfileResponse {
  const HostProfileResponse({
    required this.area,
    required this.bio,
    required this.displayName,
    required this.genres,
    required this.groupKind,
    required this.id,
    required this.instagramHandle,
    required this.mediaLinks,
    required this.members,
    required this.type,
  });
  
  factory HostProfileResponse.fromJson(Map<String, Object?> json) => _$HostProfileResponseFromJson(json);
  
  final String? area;
  final String? bio;

  /// Group name, or the owner's display name for an individual (null if not set yet)
  final String? displayName;
  final List<HostProfileResponseGenres> genres;
  final HostProfileResponseGroupKind? groupKind;
  final String id;
  final String? instagramHandle;
  final List<MediaLinkDto> mediaLinks;

  /// Owner first (admin), then accepted members. Pending invites are never listed here.
  final List<HostMemberDto> members;
  final HostProfileResponseType type;

  Map<String, Object?> toJson() => _$HostProfileResponseToJson(this);
}
