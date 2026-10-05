// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:dio/dio.dart';
import 'package:retrofit/retrofit.dart';

import '../models/admin_action_page.dart';
import '../models/reject_verification_request.dart';
import '../models/status.dart';
import '../models/venue_response.dart';
import '../models/venue_review_response.dart';
import '../models/venue_verification_item.dart';

part 'admin_client.g.dart';

@RestApi()
abstract class AdminClient {
  factory AdminClient(Dio dio, {String? baseUrl}) = _AdminClient;

  @GET('/api/v1/admin/actions')
  Future<AdminActionPage> adminActions({
    @Query('before') DateTime? before,
    @Query('beforeId') String? beforeId,
  });

  @GET('/api/v1/admin/venue-verifications')
  Future<List<VenueVerificationItem>> venueVerificationQueue({
    @Query('status') Status? status = Status.pending,
  });

  @GET('/api/v1/admin/venues/{venueId}')
  Future<VenueReviewResponse> reviewVenue({
    @Path('venueId') required String venueId,
  });

  @POST('/api/v1/admin/venues/{venueId}/approve')
  Future<VenueResponse> approveVenue({
    @Path('venueId') required String venueId,
  });

  @POST('/api/v1/admin/venues/{venueId}/reject')
  Future<VenueResponse> rejectVenue({
    @Path('venueId') required String venueId,
    @Body() required RejectVerificationRequest body,
  });
}
