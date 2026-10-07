// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'photo_upload_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PhotoUploadRequest _$PhotoUploadRequestFromJson(Map<String, dynamic> json) =>
    PhotoUploadRequest(
      contentType: json['contentType'] as String,
      sizeBytes: (json['sizeBytes'] as num).toInt(),
    );

Map<String, dynamic> _$PhotoUploadRequestToJson(PhotoUploadRequest instance) =>
    <String, dynamic>{
      'contentType': instance.contentType,
      'sizeBytes': instance.sizeBytes,
    };
