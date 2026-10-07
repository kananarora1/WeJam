// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'photo_upload_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PhotoUploadResponse _$PhotoUploadResponseFromJson(Map<String, dynamic> json) =>
    PhotoUploadResponse(
      expiresAt: DateTime.parse(json['expiresAt'] as String),
      headers: Map<String, String>.from(json['headers'] as Map),
      method: json['method'] as String,
      photoId: json['photoId'] as String,
      uploadUrl: json['uploadUrl'] as String,
    );

Map<String, dynamic> _$PhotoUploadResponseToJson(
  PhotoUploadResponse instance,
) => <String, dynamic>{
  'expiresAt': instance.expiresAt.toIso8601String(),
  'headers': instance.headers,
  'method': instance.method,
  'photoId': instance.photoId,
  'uploadUrl': instance.uploadUrl,
};
