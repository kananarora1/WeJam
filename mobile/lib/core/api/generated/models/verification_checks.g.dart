// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'verification_checks.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VerificationChecks _$VerificationChecksFromJson(Map<String, dynamic> json) =>
    VerificationChecks(
      fssaiStructureLooksValid: json['fssaiStructureLooksValid'] as bool,
      otherVenuesWithSameFssai: (json['otherVenuesWithSameFssai'] as num)
          .toInt(),
    );

Map<String, dynamic> _$VerificationChecksToJson(VerificationChecks instance) =>
    <String, dynamic>{
      'fssaiStructureLooksValid': instance.fssaiStructureLooksValid,
      'otherVenuesWithSameFssai': instance.otherVenuesWithSameFssai,
    };
