// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

part 'photo_upload_response.g.dart';

@JsonSerializable()
class PhotoUploadResponse {
  const PhotoUploadResponse({
    required this.expiresAt,
    required this.headers,
    required this.method,
    required this.photoId,
    required this.uploadUrl,
  });
  
  factory PhotoUploadResponse.fromJson(Map<String, Object?> json) => _$PhotoUploadResponseFromJson(json);
  
  final DateTime expiresAt;
  final Map<String, String> headers;
  final String method;
  final String photoId;
  final String uploadUrl;

  Map<String, Object?> toJson() => _$PhotoUploadResponseToJson(this);
}
