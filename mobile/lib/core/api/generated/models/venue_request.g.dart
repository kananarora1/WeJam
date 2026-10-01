// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'venue_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VenueRequest _$VenueRequestFromJson(Map<String, dynamic> json) => VenueRequest(
  addressLine: json['addressLine'] as String,
  city: json['city'] as String,
  latitude: (json['latitude'] as num).toDouble(),
  longitude: (json['longitude'] as num).toDouble(),
  name: json['name'] as String,
  description: json['description'] as String?,
);

Map<String, dynamic> _$VenueRequestToJson(VenueRequest instance) =>
    <String, dynamic>{
      'addressLine': instance.addressLine,
      'city': instance.city,
      'description': instance.description,
      'latitude': instance.latitude,
      'longitude': instance.longitude,
      'name': instance.name,
    };
