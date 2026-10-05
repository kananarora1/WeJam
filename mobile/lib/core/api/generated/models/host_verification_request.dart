// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'host_verification_request_id_type.dart';

part 'host_verification_request.g.dart';

@JsonSerializable()
class HostVerificationRequest {
  const HostVerificationRequest({
    required this.idType,
  });
  
  factory HostVerificationRequest.fromJson(Map<String, Object?> json) => _$HostVerificationRequestFromJson(json);
  
  final HostVerificationRequestIdType idType;

  Map<String, Object?> toJson() => _$HostVerificationRequestToJson(this);
}
