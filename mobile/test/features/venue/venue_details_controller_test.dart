import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:latlong2/latlong.dart';
import 'package:wejam/core/api/api_error.dart';
import 'package:wejam/features/venue/data/venue_repository.dart';
import 'package:wejam/features/venue/domain/cities.dart';
import 'package:wejam/features/venue/presentation/venue_details_controller.dart';

import '../../support/venue_fakes.dart';

void main() {
  late FakeVenues venues;
  late ProviderContainer container;
  const entrance = LatLng(12.9719, 77.6412);

  VenueDetailsController controller(String? id) =>
      container.read(venueDetailsControllerProvider(id).notifier);
  VenueDetailsState state(String? id) =>
      container.read(venueDetailsControllerProvider(id));

  setUp(() {
    venues = FakeVenues();
    container = ProviderContainer.test(
      overrides: [venueRepositoryProvider.overrideWithValue(venues)],
    );
  });

  /// Keeps the auto-disposed form alive like the screen would, and lets an existing venue load.
  Future<void> open(String? id) async {
    container.listen(venueDetailsControllerProvider(id), (_, _) {});
    await pumpEventQueue();
  }

  group('a new venue', () {
    setUp(() => open(null));

    test(
      'starts in Bengaluru with no pin: the city centre is never an entrance',
      () {
        expect(state(null).city.name, 'Bengaluru');
        expect(state(null).pin, isNull);
      },
    );

    test('can\'t be saved until the pin is placed', () async {
      final saved = await controller(null)
          .save(name: 'Amber Room', address: '100 Ft Road');

      expect(saved, isNull);
      expect(state(null).fieldErrors.keys, ['location']);
      expect(venues.created, isEmpty);
    });

    test(
      'name and address are required, and checked before calling the backend',
      () async {
        controller(null).placePin(entrance);

        await controller(null).save(name: '  ', address: '');

        expect(
          state(null).fieldErrors.keys,
          containsAll(['name', 'addressLine']),
        );
        expect(venues.created, isEmpty);
      },
    );

    test('saves a draft with trimmed values, the city and the pin', () async {
      controller(null).placePin(entrance);

      final saved = await controller(null)
          .save(name: ' Amber Room ', address: ' 100 Ft Road ');

      expect(saved?.id, 'v-new');
      final details = venues.created.single;
      expect(details.name, 'Amber Room');
      expect(details.addressLine, '100 Ft Road');
      expect(details.city, 'Bengaluru');
      expect(details.location, entrance);
    });

    test('picking another city clears the pin, which must be placed again', () {
      controller(null).placePin(entrance);

      controller(null)
          .selectCity(Cities.all.firstWhere((c) => c.name == 'Pune'));

      expect(state(null).city.name, 'Pune');
      expect(state(null).pin, isNull);
    });

    test('server field errors land on their fields; the pin covers latitude/longitude', () async {
      venues.saveError = const ApiFailure(
        'Invalid request content.',
        statusCode: 400,
        fieldErrors: {
          'name': 'size must be at most 80',
          'latitude': 'must be <= 90',
        },
      );
      controller(null).placePin(entrance);

      await controller(null).save(name: 'Amber Room', address: '100 Ft Road');

      expect(state(null).fieldErrors, {
        'name': 'size must be at most 80',
        'location': 'must be <= 90',
      });
      expect(state(null).error, isNull);
      expect(state(null).saving, isFalse);
    });

    test('other failures show one message', () async {
      venues.saveError = const ApiFailure("Can't reach WeJam right now.");
      controller(null).placePin(entrance);

      await controller(null).save(name: 'Amber Room', address: '100 Ft Road');

      expect(state(null).error, "Can't reach WeJam right now.");
    });
  });

  group('an existing venue', () {
    test('loads with its city and pin, and saves through update keeping everything else', () async {
      venues.stored = FakeVenues.licensed;
      await open('v1');

      expect(state('v1').loading, isFalse);
      expect(state('v1').city.name, 'Mumbai');
      expect(state('v1').pin, FakeVenues.licensed.location);

      await controller('v1')
          .save(name: 'Amber Room & Kitchen', address: '100 Ft Road');

      final (current, details) = venues.updated.single;
      // The whole venue goes back, so its license and hosting mode survive the edit.
      expect(current.fssaiNumber, '12345678901234');
      expect(current.hostingMode, 'SELF_ONLY');
      expect(details.name, 'Amber Room & Kitchen');
      expect(venues.created, isEmpty);
    });

    test('a load failure can be retried', () async {
      venues.getError = const ApiFailure("Can't reach WeJam right now.");
      await open('v1');
      expect(state('v1').loadError, "Can't reach WeJam right now.");

      venues
        ..getError = null
        ..stored = FakeVenues.licensed;
      await controller('v1').retryLoad();

      expect(state('v1').loadError, isNull);
      expect(state('v1').venue?.name, 'Amber Room');
    });
  });
}
