import 'package:latlong2/latlong.dart';
import 'package:wejam/core/api/api_error.dart';
import 'package:wejam/features/venue/data/venue_repository.dart';
import 'package:wejam/features/venue/domain/venue.dart';

class FakeVenues implements VenueRepository {
  List<VenueSummary> mine = [];

  /// What `get` returns; null = the backend says it doesn't exist.
  Venue? stored;
  Object? getError;
  Object? saveError;
  final created = <VenueDetails>[];
  final updated = <(Venue, VenueDetails)>[];

  static final licensed = Venue(
    id: 'v1',
    name: 'Amber Room',
    description: 'Upstairs',
    addressLine: '100 Ft Road',
    city: 'Mumbai',
    location: const LatLng(19.07, 72.87),
    hostingMode: 'SELF_ONLY',
    fssaiNumber: '12345678901234',
    fssaiLicenseName: 'Amber Hospitality LLP',
    fssaiValidUntil: DateTime(2030, 3, 31),
    status: VenueStatus.pending,
  );

  @override
  Future<List<VenueSummary>> myVenues() async => mine;

  @override
  Future<Venue> get(String venueId) async {
    await Future<void>.delayed(Duration.zero);
    if (getError != null) throw getError!;
    return stored ??
        (throw const ApiFailure('Venue not found', statusCode: 404));
  }

  @override
  Future<Venue> create(VenueDetails details) async {
    created.add(details);
    await Future<void>.delayed(Duration.zero);
    if (saveError != null) throw saveError!;
    return _venue('v-new', details, VenueStatus.draft);
  }

  @override
  Future<Venue> updateDetails(Venue current, VenueDetails details) async {
    updated.add((current, details));
    await Future<void>.delayed(Duration.zero);
    if (saveError != null) throw saveError!;
    return _venue(current.id, details, current.status);
  }

  static Venue _venue(String id, VenueDetails d, VenueStatus status) => Venue(
    id: id,
    name: d.name,
    description: null,
    addressLine: d.addressLine,
    city: d.city,
    location: d.location,
    hostingMode: 'OPEN',
    fssaiNumber: null,
    fssaiLicenseName: null,
    fssaiValidUntil: null,
    status: status,
  );
}
