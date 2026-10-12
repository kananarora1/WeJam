import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:latlong2/latlong.dart';

import '../../../core/api/api_error.dart';
import '../data/venue_repository.dart';
import '../domain/cities.dart';
import '../domain/venue.dart';
import 'my_venues_provider.dart';

/// Onboarding step 1 (design 7.1) for one venue; the argument is its id, or null for a new one.
/// Auto-disposed, so the form starts fresh each visit.
final venueDetailsControllerProvider = NotifierProvider.autoDispose
    .family<VenueDetailsController, VenueDetailsState, String?>(
      VenueDetailsController.new,
    );

@immutable
class VenueDetailsState {
  const VenueDetailsState({
    required this.city,
    this.venue,
    this.loading = false,
    this.loadError,
    this.pin,
    this.saving = false,
    this.error,
    this.fieldErrors = const {},
  });

  /// The saved venue being edited; null for a new venue (and while an existing one loads).
  final Venue? venue;
  final bool loading;
  final String? loadError;
  final City city;

  /// Null until the owner drags the map: the city centre is never a real entrance.
  final LatLng? pin;
  final bool saving;

  /// A problem that isn't about one field (e.g. no connection).
  final String? error;

  /// Keyed `name`, `addressLine`, `city`, `location`.
  final Map<String, String> fieldErrors;

  VenueDetailsState copyWith({
    Venue? venue,
    bool? loading,
    Object? loadError = _keep,
    City? city,
    Object? pin = _keep,
    bool? saving,
    Object? error = _keep,
    Map<String, String>? fieldErrors,
  }) => VenueDetailsState(
    venue: venue ?? this.venue,
    loading: loading ?? this.loading,
    loadError: loadError == _keep ? this.loadError : loadError as String?,
    city: city ?? this.city,
    pin: pin == _keep ? this.pin : pin as LatLng?,
    saving: saving ?? this.saving,
    error: error == _keep ? this.error : error as String?,
    fieldErrors: fieldErrors ?? this.fieldErrors,
  );
}

const _keep = Object();

class VenueDetailsController extends Notifier<VenueDetailsState> {
  VenueDetailsController(this.venueId);

  /// Null for a venue that hasn't been saved yet.
  final String? venueId;

  /// Same limits as the backend's `VenueRequest`.
  static const nameMaxLength = 80;
  static const addressMaxLength = 200;

  @override
  VenueDetailsState build() {
    if (venueId == null) return VenueDetailsState(city: Cities.initial);
    Future.microtask(_load);
    return VenueDetailsState(city: Cities.initial, loading: true);
  }

  Future<void> retryLoad() async {
    state = state.copyWith(loading: true, loadError: null);
    await _load();
  }

  Future<void> _load() async {
    try {
      final venue = await ref.read(venueRepositoryProvider).get(venueId!);
      if (!ref.mounted) return;
      state = VenueDetailsState(
        venue: venue,
        city: Cities.named(venue.city, fallbackCenter: venue.location),
        pin: venue.location,
      );
    } on ApiFailure catch (e) {
      if (!ref.mounted) return;
      state = state.copyWith(loading: false, loadError: e.message);
    }
  }

  /// A different city means a different place: the pin has to be placed again.
  void selectCity(City city) {
    if (city.name == state.city.name) return;
    state = state.copyWith(
      city: city,
      pin: null,
      fieldErrors: _without(state.fieldErrors, 'city'),
    );
  }

  void placePin(LatLng at) => state = state.copyWith(
    pin: at,
    fieldErrors: _without(state.fieldErrors, 'location'),
  );

  void clearFieldError(String field) =>
      state = state.copyWith(fieldErrors: _without(state.fieldErrors, field));

  static String? validateName(String raw) {
    final name = raw.trim();
    if (name.isEmpty) return 'Enter your venue’s name.';
    if (name.length > nameMaxLength) {
      return 'Keep it to $nameMaxLength characters.';
    }
    return null;
  }

  static String? validateAddress(String raw) {
    final address = raw.trim();
    if (address.isEmpty) return 'Enter the street address.';
    if (address.length > addressMaxLength) {
      return 'Keep it to $addressMaxLength characters.';
    }
    return null;
  }

  /// Saves the draft (creating it the first time). Returns the saved venue, or null if it couldn't
  /// be saved — the state then says why.
  Future<Venue?> save({required String name, required String address}) async {
    if (state.saving || state.loading) return null;
    final pin = state.pin;
    final invalid = {
      'name': ?validateName(name),
      'addressLine': ?validateAddress(address),
      if (pin == null)
        'location': 'Drag the map until the pin sits on your entrance.',
    };
    if (invalid.isNotEmpty || pin == null) {
      state = state.copyWith(fieldErrors: invalid, error: null);
      return null;
    }

    state = state.copyWith(saving: true, error: null, fieldErrors: const {});
    final details = VenueDetails(
      name: name.trim(),
      addressLine: address.trim(),
      city: state.city.name,
      location: pin,
    );
    try {
      final venues = ref.read(venueRepositoryProvider);
      final current = state.venue;
      final saved = current == null
          ? await venues.create(details)
          : await venues.updateDetails(current, details);
      ref.invalidate(myVenuesProvider);
      if (ref.mounted) state = state.copyWith(venue: saved, saving: false);
      return saved;
    } on ApiFailure catch (e) {
      if (!ref.mounted) return null;
      final fields = {
        for (final MapEntry(:key, :value) in e.fieldErrors.entries)
          (key == 'latitude' || key == 'longitude' ? 'location' : key): value,
      };
      state = state.copyWith(
        saving: false,
        fieldErrors: fields,
        error: fields.isEmpty ? e.message : null,
      );
      return null;
    }
  }

  static Map<String, String> _without(
    Map<String, String> errors,
    String field,
  ) => errors.containsKey(field) ? ({...errors}..remove(field)) : errors;
}
