// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

part 'photo_upload_request.g.dart';

@JsonSerializable()
class PhotoUploadRequest {
  const PhotoUploadRequest({
    required this.contentType,
    required this.sizeBytes,
  });
  
  factory PhotoUploadRequest.fromJson(Map<String, Object?> json) => _$PhotoUploadRequestFromJson(json);
  
  /// image/jpeg, image/png or image/webp
  final String contentType;

  /// Exact file size; max 5 MB (compress on the phone first)
  final int sizeBytes;

  Map<String, Object?> toJson() => _$PhotoUploadRequestToJson(this);
}
