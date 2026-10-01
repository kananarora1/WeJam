// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

part 'space_request.g.dart';

@JsonSerializable()
class SpaceRequest {
  const SpaceRequest({
    required this.capacity,
    required this.name,
  });
  
  factory SpaceRequest.fromJson(Map<String, Object?> json) => _$SpaceRequestFromJson(json);
  
  final int capacity;
  final String name;

  Map<String, Object?> toJson() => _$SpaceRequestToJson(this);
}
