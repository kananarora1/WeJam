// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:json_annotation/json_annotation.dart';

import 'space_response.dart';

part 'venue_response.g.dart';

@JsonSerializable()
class VenueResponse {
  const VenueResponse({
    required this.addressLine,
    required this.city,
    required this.description,
    required this.id,
    required this.latitude,
    required this.longitude,
    required this.name,
    required this.spaces,
  });
  
  factory VenueResponse.fromJson(Map<String, Object?> json) => _$VenueResponseFromJson(json);
  
  final String addressLine;
  final String city;
  final String? description;
  final String id;
  final double latitude;
  final double longitude;
  final String name;
  final List<SpaceResponse> spaces;

  Map<String, Object?> toJson() => _$VenueResponseToJson(this);
}
