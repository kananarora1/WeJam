// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'host_profile_request_genres.dart';
import 'host_profile_request_group_kind.dart';
import 'host_profile_request_type.dart';
import 'media_link_dto.dart';

part 'host_profile_request.g.dart';

@JsonSerializable()
class HostProfileRequest {
  const HostProfileRequest({
    required this.genres,
    required this.mediaLinks,
    required this.type,
    this.area,
    this.bio,
    this.groupKind,
    this.groupName,
    this.instagramHandle,
  });
  
  factory HostProfileRequest.fromJson(Map<String, Object?> json) => _$HostProfileRequestFromJson(json);
  
  final String? area;
  final String? bio;
  final List<HostProfileRequestGenres> genres;

  /// Required when type is GROUP
  final HostProfileRequestGroupKind? groupKind;

  /// Required when type is GROUP
  final String? groupName;

  /// Instagram username, with or without @
  final String? instagramHandle;
  final List<MediaLinkDto> mediaLinks;
  final HostProfileRequestType type;

  Map<String, Object?> toJson() => _$HostProfileRequestToJson(this);
}
