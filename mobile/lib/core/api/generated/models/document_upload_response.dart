// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

part 'document_upload_response.g.dart';

@JsonSerializable()
class DocumentUploadResponse {
  const DocumentUploadResponse({
    required this.documentId,
    required this.expiresAt,
    required this.headers,
    required this.method,
    required this.uploadUrl,
  });
  
  factory DocumentUploadResponse.fromJson(Map<String, Object?> json) => _$DocumentUploadResponseFromJson(json);
  
  final String documentId;
  final DateTime expiresAt;

  /// Headers that are part of the signature (e.g. Content-Type, Content-Length)
  final Map<String, String> headers;
  final String method;
  final String uploadUrl;

  Map<String, Object?> toJson() => _$DocumentUploadResponseToJson(this);
}
