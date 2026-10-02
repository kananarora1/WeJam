// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'space_request_sound_policy.dart';

part 'space_request.g.dart';

@JsonSerializable()
class SpaceRequest {
  const SpaceRequest({
    required this.capacity,
    required this.name,
    required this.soundPolicy,
    this.houseRules,
    this.soundCurfew,
  });
  
  factory SpaceRequest.fromJson(Map<String, Object?> json) => _$SpaceRequestFromJson(json);
  
  final int capacity;

  /// Shown on every event at this space
  final String? houseRules;
  final String name;

  /// Local time (venue time zone) events here must end by; null = no curfew
  final String? soundCurfew;
  final SpaceRequestSoundPolicy soundPolicy;

  Map<String, Object?> toJson() => _$SpaceRequestToJson(this);
}
