import 'package:flutter/foundation.dart';
import 'package:latlong2/latlong.dart';

/// Draft → (submit) → pending → verified | rejected → (resubmit) → pending.
enum VenueStatus { draft, pending, verified, rejected, unknown }

@immutable
class VenueSummary {
  const VenueSummary({
    required this.id,
    required this.name,
    required this.city,
    required this.status,
  });

  final String id;
  final String name;
  final String city;
  final VenueStatus status;
}

/// A venue as its owner edits it. Fields this app doesn't edit yet are carried along, because
/// the update API replaces the whole venue.
@immutable
class Venue {
  const Venue({
    required this.id,
    required this.name,
    required this.description,
    required this.addressLine,
    required this.city,
    required this.location,
    required this.hostingMode,
    required this.fssaiNumber,
    required this.fssaiLicenseName,
    required this.fssaiValidUntil,
    required this.status,
  });

  final String id;
  final String name;
  final String? description;
  final String addressLine;
  final String city;
  final LatLng location;

  /// The API's value (`OPEN` / `SELF_ONLY`), passed back untouched until the hosting settings screen exists.
  final String hostingMode;
  final String? fssaiNumber;
  final String? fssaiLicenseName;
  final DateTime? fssaiValidUntil;
  final VenueStatus status;
}

/// What onboarding step 1 (design 7.1) collects.
@immutable
class VenueDetails {
  const VenueDetails({
    required this.name,
    required this.addressLine,
    required this.city,
    required this.location,
  });

  final String name;
  final String addressLine;
  final String city;
  final LatLng location;
}
