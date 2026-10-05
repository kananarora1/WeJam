// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'document_response_type.dart';

part 'document_response.g.dart';

@JsonSerializable()
class DocumentResponse {
  const DocumentResponse({
    required this.contentType,
    required this.downloadUrl,
    required this.downloadUrlExpiresAt,
    required this.id,
    required this.sizeBytes,
    required this.type,
    required this.uploadedAt,
  });
  
  factory DocumentResponse.fromJson(Map<String, Object?> json) => _$DocumentResponseFromJson(json);
  
  final String contentType;
  final String downloadUrl;
  final DateTime downloadUrlExpiresAt;
  final String id;
  final int sizeBytes;
  final DocumentResponseType type;
  final DateTime uploadedAt;

  Map<String, Object?> toJson() => _$DocumentResponseToJson(this);
}
