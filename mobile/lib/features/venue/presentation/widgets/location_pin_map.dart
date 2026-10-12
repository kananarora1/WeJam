import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

import '../../../../core/theme/tokens.dart';

/// "Location pin · drag to your entrance" (design 7.1): the pin stays in the middle and the owner
/// drags the map underneath it, like ride-hailing apps. Reports the centre after each user drag.
///
/// Tiles are OpenStreetMap's, recoloured to the app's warm dark palette. OSM's usage policy is fine
/// for development; production needs a tile provider.
class LocationPinMap extends StatelessWidget {
  const LocationPinMap({
    super.key,
    required this.initialCenter,
    required this.zoomedIn,
    required this.onMoved,
    required this.onTouch,
  });

  final LatLng initialCenter;

  /// Street level when a pin already exists; city level otherwise.
  final bool zoomedIn;
  final ValueChanged<LatLng> onMoved;

  /// True while a finger is on the map, so the page around it can stop scrolling.
  final ValueChanged<bool> onTouch;

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    final smallPrint = Theme.of(context).textTheme.labelSmall?.copyWith(
      fontSize: 10,
      letterSpacing: 0.2,
      color: tokens.textPrimary.withValues(alpha: 0.5),
    );
    return Listener(
      onPointerDown: (_) => onTouch(true),
      onPointerUp: (_) => onTouch(false),
      onPointerCancel: (_) => onTouch(false),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(AppRadii.md),
        child: DecoratedBox(
          position: DecorationPosition.foreground,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(AppRadii.md),
            border: Border.all(color: tokens.hair),
          ),
          child: Stack(
            children: [
              FlutterMap(
                options: MapOptions(
                  initialCenter: initialCenter,
                  initialZoom: zoomedIn ? 16 : 12,
                  minZoom: 4,
                  maxZoom: 19,
                  backgroundColor: tokens.surface1,
                  interactionOptions: const InteractionOptions(
                    // A rotated map makes "find your entrance" harder, not easier.
                    flags: InteractiveFlag.all & ~InteractiveFlag.rotate,
                  ),
                  onPositionChanged: (camera, hasGesture) {
                    if (hasGesture) onMoved(camera.center);
                  },
                ),
                children: [
                  TileLayer(
                    urlTemplate:
                        'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
                    userAgentPackageName: 'com.kananarora.wejam',
                    tileBuilder: _warmDarkTile,
                  ),
                ],
              ),
              // The pin's tip marks the centre, so lift the icon by half its height.
              IgnorePointer(
                child: Center(
                  child: Transform.translate(
                    offset: const Offset(0, -18),
                    child: Icon(
                      Icons.location_on,
                      size: 36,
                      color: tokens.amberText,
                      shadows: const [Shadow(blurRadius: 8)],
                    ),
                  ),
                ),
              ),
              // OSM's licence requires the credit to stay visible on the map.
              Positioned(
                left: AppSpace.s2 + 2,
                bottom: AppSpace.s2,
                child: IgnorePointer(
                  child: Text(
                    '© OpenStreetMap contributors',
                    style: smallPrint,
                  ),
                ),
              ),
              Positioned(
                right: AppSpace.s2 + 2,
                bottom: AppSpace.s2,
                child: IgnorePointer(
                  child: Text('drag to your entrance', style: smallPrint),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  /// Light OSM tiles → warm charcoal: luminance inverted (labels stay readable), then scaled into
  /// the dark range with a slight amber cast. Land lands near #1A1613.
  static Widget _warmDarkTile(BuildContext _, Widget tile, TileImage _) =>
      ColorFiltered(
        colorFilter: const ColorFilter.matrix(<double>[
          -0.1276, -0.4291, -0.0433, 0, 171, //
          -0.1106, -0.3719, -0.0375, 0, 147.6, //
          -0.0957, -0.3218, -0.0325, 0, 127.75, //
          0, 0, 0, 1, 0, //
        ]),
        child: tile,
      );
}
