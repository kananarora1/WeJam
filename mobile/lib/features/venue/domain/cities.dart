import 'package:latlong2/latlong.dart';

/// Cities offered in venue onboarding. The backend accepts any city; this list only drives the
/// picker and where the map starts. Bengaluru first: it's where WeJam launches.
class City {
  const City(this.name, this.center);

  final String name;
  final LatLng center;
}

abstract final class Cities {
  static const all = [
    City('Bengaluru', LatLng(12.9716, 77.5946)),
    City('Mumbai', LatLng(19.0760, 72.8777)),
    City('Delhi NCR', LatLng(28.6139, 77.2090)),
    City('Hyderabad', LatLng(17.3850, 78.4867)),
    City('Pune', LatLng(18.5204, 73.8567)),
    City('Chennai', LatLng(13.0827, 80.2707)),
    City('Kolkata', LatLng(22.5726, 88.3639)),
  ];

  static City get initial => all.first;

  /// A city saved earlier that's no longer in the list still shows up rather than being swapped out.
  static City named(String name, {required LatLng fallbackCenter}) =>
      all.firstWhere(
        (c) => c.name == name,
        orElse: () => City(name, fallbackCenter),
      );
}
