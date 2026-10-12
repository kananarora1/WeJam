import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';

import '../../../core/api/api_error.dart';
import '../../../core/api/api_providers.dart';
import '../../../core/api/generated/export.dart' as api;
import '../domain/venue.dart';

final venueRepositoryProvider = Provider<VenueRepository>(
  (ref) => VenueRepository(ref.watch(apiProvider)),
);

/// The venue endpoints, translated to domain types and [ApiFailure]s.
class VenueRepository {
  VenueRepository(this._api);

  final api.WeJamApi _api;

  Future<List<VenueSummary>> myVenues() => _call(() async {
    final venues = await _api.venues.myVenues();
    return [
      for (final v in venues)
        VenueSummary(
          id: v.id,
          name: v.name,
          city: v.city,
          status: _status(v.verificationStatus.json),
        ),
    ];
  });

  Future<Venue> get(String venueId) =>
      _call(() async => _toVenue(await _api.venues.getVenue(venueId: venueId)));

  /// Saves onboarding step 1 as a new private draft. Hosting mode starts open; it's set properly
  /// after verification (design 7.7).
  Future<Venue> create(VenueDetails details) => _call(
    () async => _toVenue(
      await _api.venues.createVenue(
        body: api.VenueRequest(
          name: details.name,
          addressLine: details.addressLine,
          city: details.city,
          latitude: details.location.latitude,
          longitude: details.location.longitude,
          hostingMode: api.VenueRequestHostingMode.open,
        ),
      ),
    ),
  );

  /// The update API replaces the whole venue, so everything step 1 doesn't edit is sent back as loaded.
  Future<Venue> updateDetails(Venue current, VenueDetails details) => _call(
    () async => _toVenue(
      await _api.venues.updateVenue(
        venueId: current.id,
        body: api.VenueRequest(
          name: details.name,
          addressLine: details.addressLine,
          city: details.city,
          latitude: details.location.latitude,
          longitude: details.location.longitude,
          description: current.description,
          hostingMode: api.VenueRequestHostingMode.values.firstWhere(
            (m) => m.json == current.hostingMode,
            orElse: () => api.VenueRequestHostingMode.open,
          ),
          fssaiNumber: current.fssaiNumber,
          fssaiLicenseName: current.fssaiLicenseName,
          fssaiValidUntil: current.fssaiValidUntil,
        ),
      ),
    ),
  );

  static Future<T> _call<T>(Future<T> Function() request) async {
    try {
      return await request();
    } on DioException catch (e) {
      throw ApiFailure.fromDio(e);
    }
  }

  static Venue _toVenue(api.VenueResponse v) => Venue(
    id: v.id,
    name: v.name,
    description: v.description,
    addressLine: v.addressLine,
    city: v.city,
    location: LatLng(v.latitude, v.longitude),
    hostingMode: v.hostingMode.json ?? 'OPEN',
    fssaiNumber: v.fssaiNumber,
    fssaiLicenseName: v.fssaiLicenseName,
    fssaiValidUntil: v.fssaiValidUntil,
    status: _status(v.verificationStatus.json),
  );

  static VenueStatus _status(String? json) => switch (json) {
    'DRAFT' => VenueStatus.draft,
    'PENDING' => VenueStatus.pending,
    'VERIFIED' => VenueStatus.verified,
    'REJECTED' => VenueStatus.rejected,
    _ => VenueStatus.unknown, // a status added on the backend after this app version shipped
  };
}
