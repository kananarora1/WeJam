// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'add_role_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AddRoleRequest _$AddRoleRequestFromJson(Map<String, dynamic> json) =>
    AddRoleRequest(role: AddRoleRequestRole.fromJson(json['role'] as String));

Map<String, dynamic> _$AddRoleRequestToJson(AddRoleRequest instance) =>
    <String, dynamic>{'role': _$AddRoleRequestRoleEnumMap[instance.role]!};

const _$AddRoleRequestRoleEnumMap = {
  AddRoleRequestRole.user: 'USER',
  AddRoleRequestRole.host: 'HOST',
  AddRoleRequestRole.venueAdmin: 'VENUE_ADMIN',
  AddRoleRequestRole.$unknown: r'$unknown',
};
