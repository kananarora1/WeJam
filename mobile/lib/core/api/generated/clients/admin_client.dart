// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:dio/dio.dart';
import 'package:retrofit/retrofit.dart';

import '../models/admin_action_page.dart';
import '../models/host_review_response.dart';
import '../models/host_verification_item.dart';
import '../models/reject_host_verification_request.dart';
import '../models/reject_verification_request.dart';
import '../models/status.dart';
import '../models/status2.dart';
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

  @GET('/api/v1/admin/host-verifications')
  Future<List<HostVerificationItem>> hostVerificationQueue({
    @Query('status') Status? status = Status.pending,
  });

  @GET('/api/v1/admin/hosts/{hostProfileId}')
  Future<HostReviewResponse> reviewHost({
    @Path('hostProfileId') required String hostProfileId,
  });

  @POST('/api/v1/admin/hosts/{hostProfileId}/approve')
  Future<HostReviewResponse> approveHost({
    @Path('hostProfileId') required String hostProfileId,
  });

  @POST('/api/v1/admin/hosts/{hostProfileId}/reject')
  Future<HostReviewResponse> rejectHost({
    @Path('hostProfileId') required String hostProfileId,
    @Body() required RejectHostVerificationRequest body,
  });

  @GET('/api/v1/admin/venue-verifications')
  Future<List<VenueVerificationItem>> venueVerificationQueue({
    @Query('status') Status2? status = Status2.pending,
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
