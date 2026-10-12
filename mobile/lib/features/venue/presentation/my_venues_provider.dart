import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/venue_repository.dart';
import '../domain/venue.dart';

/// The signed-in owner's venues. Invalidated after a save so the list reflects it.
final myVenuesProvider = FutureProvider.autoDispose<List<VenueSummary>>(
  (ref) => ref.watch(venueRepositoryProvider).myVenues(),
);
