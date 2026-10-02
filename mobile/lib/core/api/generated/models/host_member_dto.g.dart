// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'host_member_dto.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HostMemberDto _$HostMemberDtoFromJson(Map<String, dynamic> json) =>
    HostMemberDto(
      admin: json['admin'] as bool,
      displayName: json['displayName'] as String?,
      userId: json['userId'] as String,
    );

Map<String, dynamic> _$HostMemberDtoToJson(HostMemberDto instance) =>
    <String, dynamic>{
      'admin': instance.admin,
      'displayName': instance.displayName,
      'userId': instance.userId,
    };
