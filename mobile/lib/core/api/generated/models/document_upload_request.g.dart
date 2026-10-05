// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'document_upload_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DocumentUploadRequest _$DocumentUploadRequestFromJson(
  Map<String, dynamic> json,
) => DocumentUploadRequest(
  contentType: json['contentType'] as String,
  sizeBytes: (json['sizeBytes'] as num).toInt(),
  type: DocumentUploadRequestType.fromJson(json['type'] as String),
);

Map<String, dynamic> _$DocumentUploadRequestToJson(
  DocumentUploadRequest instance,
) => <String, dynamic>{
  'contentType': instance.contentType,
  'sizeBytes': instance.sizeBytes,
  'type': _$DocumentUploadRequestTypeEnumMap[instance.type]!,
};

const _$DocumentUploadRequestTypeEnumMap = {
  DocumentUploadRequestType.fssaiCertificate: 'FSSAI_CERTIFICATE',
  DocumentUploadRequestType.leaseAgreement: 'LEASE_AGREEMENT',
  DocumentUploadRequestType.idFront: 'ID_FRONT',
  DocumentUploadRequestType.idBack: 'ID_BACK',
  DocumentUploadRequestType.$unknown: r'$unknown',
};
