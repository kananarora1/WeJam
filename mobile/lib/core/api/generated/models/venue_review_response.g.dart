// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'venue_review_response.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

VenueReviewResponse _$VenueReviewResponseFromJson(Map<String, dynamic> json) =>
    VenueReviewResponse(
      checks: VerificationChecks.fromJson(
        json['checks'] as Map<String, dynamic>,
      ),
      ownerName: json['ownerName'] as String?,
      ownerPhone: json['ownerPhone'] as String?,
      venue: VenueResponse.fromJson(json['venue'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$VenueReviewResponseToJson(
  VenueReviewResponse instance,
) => <String, dynamic>{
  'checks': instance.checks,
  'ownerName': instance.ownerName,
  'ownerPhone': instance.ownerPhone,
  'venue': instance.venue,
};
