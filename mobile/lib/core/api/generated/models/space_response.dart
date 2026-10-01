// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

part 'space_response.g.dart';

@JsonSerializable()
class SpaceResponse {
  const SpaceResponse({
    required this.capacity,
    required this.id,
    required this.name,
  });
  
  factory SpaceResponse.fromJson(Map<String, Object?> json) => _$SpaceResponseFromJson(json);
  
  final int capacity;
  final String id;
  final String name;

  Map<String, Object?> toJson() => _$SpaceResponseToJson(this);
}
