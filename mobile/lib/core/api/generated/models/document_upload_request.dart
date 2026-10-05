// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'document_upload_request_type.dart';

part 'document_upload_request.g.dart';

@JsonSerializable()
class DocumentUploadRequest {
  const DocumentUploadRequest({
    required this.contentType,
    required this.sizeBytes,
    required this.type,
  });
  
  factory DocumentUploadRequest.fromJson(Map<String, Object?> json) => _$DocumentUploadRequestFromJson(json);
  
  /// image/jpeg, image/png or application/pdf
  final String contentType;

  /// Exact file size; max 10 MB
  final int sizeBytes;
  final DocumentUploadRequestType type;

  Map<String, Object?> toJson() => _$DocumentUploadRequestToJson(this);
}
