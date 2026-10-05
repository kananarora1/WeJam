// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'document_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DocumentResponse _$DocumentResponseFromJson(Map<String, dynamic> json) =>
    DocumentResponse(
      contentType: json['contentType'] as String,
      downloadUrl: json['downloadUrl'] as String,
      downloadUrlExpiresAt: DateTime.parse(
        json['downloadUrlExpiresAt'] as String,
      ),
      id: json['id'] as String,
      sizeBytes: (json['sizeBytes'] as num).toInt(),
      type: DocumentResponseType.fromJson(json['type'] as String),
      uploadedAt: DateTime.parse(json['uploadedAt'] as String),
    );

Map<String, dynamic> _$DocumentResponseToJson(DocumentResponse instance) =>
    <String, dynamic>{
      'contentType': instance.contentType,
      'downloadUrl': instance.downloadUrl,
      'downloadUrlExpiresAt': instance.downloadUrlExpiresAt.toIso8601String(),
      'id': instance.id,
      'sizeBytes': instance.sizeBytes,
      'type': _$DocumentResponseTypeEnumMap[instance.type]!,
      'uploadedAt': instance.uploadedAt.toIso8601String(),
    };

const _$DocumentResponseTypeEnumMap = {
  DocumentResponseType.fssaiCertificate: 'FSSAI_CERTIFICATE',
  DocumentResponseType.leaseAgreement: 'LEASE_AGREEMENT',
  DocumentResponseType.$unknown: r'$unknown',
};
