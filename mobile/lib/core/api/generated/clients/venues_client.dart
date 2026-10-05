// coverage:ignore-file
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, unused_import, invalid_annotation_target, unnecessary_import

import 'package:dio/dio.dart';
import 'package:retrofit/retrofit.dart';

import '../models/space_request.dart';
import '../models/space_response.dart';
import '../models/venue_request.dart';
import '../models/venue_response.dart';
import '../models/venue_summary.dart';

part 'venues_client.g.dart';

@RestApi()
abstract class VenuesClient {
  factory VenuesClient(Dio dio, {String? baseUrl}) = _VenuesClient;

  @GET('/api/v1/me/venues')
  Future<List<VenueSummary>> myVenues();

  @POST('/api/v1/venues')
  Future<VenueResponse> createVenue({
    @Body() required VenueRequest body,
  });

  @DELETE('/api/v1/venues/{venueId}')
  Future<void> deleteVenue({
    @Path('venueId') required String venueId,
  });

  @GET('/api/v1/venues/{venueId}')
  Future<VenueResponse> getVenue({
    @Path('venueId') required String venueId,
  });

  @PUT('/api/v1/venues/{venueId}')
  Future<VenueResponse> updateVenue({
    @Path('venueId') required String venueId,
    @Body() required VenueRequest body,
  });

  @POST('/api/v1/venues/{venueId}/resubmit')
  Future<VenueResponse> resubmitVenue({
    @Path('venueId') required String venueId,
  });

  @POST('/api/v1/venues/{venueId}/spaces')
  Future<SpaceResponse> createSpace({
    @Path('venueId') required String venueId,
    @Body() required SpaceRequest body,
  });

  @DELETE('/api/v1/venues/{venueId}/spaces/{spaceId}')
  Future<void> deleteSpace({
    @Path('venueId') required String venueId,
    @Path('spaceId') required String spaceId,
  });

  @PUT('/api/v1/venues/{venueId}/spaces/{spaceId}')
  Future<SpaceResponse> updateSpace({
    @Path('venueId') required String venueId,
    @Path('spaceId') required String spaceId,
    @Body() required SpaceRequest body,
  });
}
