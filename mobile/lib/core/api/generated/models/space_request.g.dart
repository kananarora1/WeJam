// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'space_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SpaceRequest _$SpaceRequestFromJson(Map<String, dynamic> json) => SpaceRequest(
  capacity: (json['capacity'] as num).toInt(),
  name: json['name'] as String,
);

Map<String, dynamic> _$SpaceRequestToJson(SpaceRequest instance) =>
    <String, dynamic>{'capacity': instance.capacity, 'name': instance.name};
