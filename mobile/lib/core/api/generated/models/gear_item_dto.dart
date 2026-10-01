// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

part 'gear_item_dto.g.dart';

@JsonSerializable()
class GearItemDto {
  const GearItemDto({
    required this.details,
    required this.name,
  });
  
  factory GearItemDto.fromJson(Map<String, Object?> json) => _$GearItemDtoFromJson(json);
  
  final String? details;
  final String name;

  Map<String, Object?> toJson() => _$GearItemDtoToJson(this);
}
