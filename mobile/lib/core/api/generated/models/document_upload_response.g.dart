// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'document_upload_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DocumentUploadResponse _$DocumentUploadResponseFromJson(
  Map<String, dynamic> json,
) => DocumentUploadResponse(
  documentId: json['documentId'] as String,
  expiresAt: DateTime.parse(json['expiresAt'] as String),
  headers: Map<String, String>.from(json['headers'] as Map),
  method: json['method'] as String,
  uploadUrl: json['uploadUrl'] as String,
);

Map<String, dynamic> _$DocumentUploadResponseToJson(
  DocumentUploadResponse instance,
) => <String, dynamic>{
  'documentId': instance.documentId,
  'expiresAt': instance.expiresAt.toIso8601String(),
  'headers': instance.headers,
  'method': instance.method,
  'uploadUrl': instance.uploadUrl,
};
