// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'me_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MeResponse _$MeResponseFromJson(Map<String, dynamic> json) => MeResponse(
  displayName: json['displayName'] as String?,
  id: json['id'] as String,
  phone: json['phone'] as String?,
  roles: (json['roles'] as List<dynamic>)
      .map((e) => MeResponseRoles.fromJson(e as String))
      .toList(),
);

Map<String, dynamic> _$MeResponseToJson(MeResponse instance) =>
    <String, dynamic>{
      'displayName': instance.displayName,
      'id': instance.id,
      'phone': instance.phone,
      'roles': instance.roles.map((e) => _$MeResponseRolesEnumMap[e]!).toList(),
    };

const _$MeResponseRolesEnumMap = {
  MeResponseRoles.user: 'USER',
  MeResponseRoles.host: 'HOST',
  MeResponseRoles.venueAdmin: 'VENUE_ADMIN',
  MeResponseRoles.$unknown: r'$unknown',
};
