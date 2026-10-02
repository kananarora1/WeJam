// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

part 'media_link_dto.g.dart';

@JsonSerializable()
class MediaLinkDto {
  const MediaLinkDto({
    required this.title,
    required this.url,
  });
  
  factory MediaLinkDto.fromJson(Map<String, Object?> json) => _$MediaLinkDtoFromJson(json);
  
  final String? title;
  final String url;

  Map<String, Object?> toJson() => _$MediaLinkDtoToJson(this);
}
