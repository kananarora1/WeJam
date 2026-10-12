import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/router/route_resolver.dart';
import '../../../core/theme/tokens.dart';
import '../../../core/widgets/eq_bars.dart';
import '../../../core/widgets/primary_button.dart';
import '../../../core/widgets/step_progress.dart';
import '../domain/cities.dart';
import 'venue_details_controller.dart';
import 'widgets/location_pin_map.dart';
import 'widgets/venue_status_badge.dart';

/// Venue onboarding step 1 of 4 (design 7.1): name, city, address and the entrance pin. Continue
/// saves a private draft. Photos (step 2) come next.
class VenueDetailsScreen extends ConsumerStatefulWidget {
  const VenueDetailsScreen({super.key, required this.venueId});

  /// Null for a new venue.
  final String? venueId;

  @override
  ConsumerState<VenueDetailsScreen> createState() => _VenueDetailsScreenState();
}

class _VenueDetailsScreenState extends ConsumerState<VenueDetailsScreen> {
  final _name = TextEditingController();
  final _address = TextEditingController();
  var _touchingMap = false;

  VenueDetailsController get _controller =>
      ref.read(venueDetailsControllerProvider(widget.venueId).notifier);

  @override
  void dispose() {
    _name.dispose();
    _address.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    FocusScope.of(context).unfocus();
    final saved = await _controller.save(
      name: _name.text,
      address: _address.text,
    );
    if (saved == null || !mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Draft saved. Photos come next.')),
    );
    if (context.canPop()) {
      context.pop();
    } else {
      context.go(Routes.venue);
    }
  }

  void _close() => context.canPop() ? context.pop() : context.go(Routes.venue);

  @override
  Widget build(BuildContext context) {
    final provider = venueDetailsControllerProvider(widget.venueId);
    // Fill the fields once, when an existing venue finishes loading.
    ref.listen(provider.select((s) => s.venue), (previous, venue) {
      if (previous == null && venue != null) {
        _name.text = venue.name;
        _address.text = venue.addressLine;
      }
    });
    final state = ref.watch(provider);

    return Scaffold(
      body: SafeArea(
        child: switch (state) {
          VenueDetailsState(loading: true) => Center(
            child: EqBars(color: context.tokens.amber),
          ),
          VenueDetailsState(:final loadError?) => _LoadFailed(
            message: loadError,
            onRetry: _controller.retryLoad,
            onClose: _close,
          ),
          _ => _form(context, state),
        },
      ),
    );
  }

  Widget _form(BuildContext context, VenueDetailsState state) {
    final tokens = context.tokens;
    final textTheme = Theme.of(context).textTheme;
    final venue = state.venue;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(
            AppSpace.s3,
            AppSpace.s2,
            AppSpace.s5,
            0,
          ),
          child: Row(
            children: [
              IconButton(
                onPressed: _close,
                tooltip: 'Close',
                icon: const Icon(Icons.close_rounded),
              ),
              const Spacer(),
              if (venue != null) VenueStatusBadge(venue.status),
            ],
          ),
        ),
        Expanded(
          child: SingleChildScrollView(
            physics: _touchingMap ? const NeverScrollableScrollPhysics() : null,
            padding: const EdgeInsets.fromLTRB(
              AppSpace.s5,
              AppSpace.s3,
              AppSpace.s5,
              AppSpace.s5,
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const StepProgress(step: 1, of: 4),
                const SizedBox(height: AppSpace.s4),
                Text(
                  'Step 1 of 4',
                  style: textTheme.bodySmall?.copyWith(
                    color: tokens.textTertiary,
                  ),
                ),
                const SizedBox(height: AppSpace.s1),
                Text('Your venue', style: textTheme.displayMedium),
                const SizedBox(height: AppSpace.s5),
                _Label('Venue name'),
                TextField(
                  controller: _name,
                  enabled: !state.saving,
                  textCapitalization: TextCapitalization.words,
                  textInputAction: TextInputAction.next,
                  inputFormatters: [
                    LengthLimitingTextInputFormatter(
                      VenueDetailsController.nameMaxLength,
                    ),
                  ],
                  style: textTheme.bodyLarge,
                  decoration: InputDecoration(
                    hintText: 'e.g. Amber Room',
                    errorText: state.fieldErrors['name'],
                  ),
                  onChanged: (_) => _controller.clearFieldError('name'),
                ),
                const SizedBox(height: AppSpace.s3),
                _Label('City'),
                DropdownButtonFormField<String>(
                  initialValue: state.city.name,
                  items: [
                    for (final city in {...Cities.all, state.city})
                      DropdownMenuItem(
                        value: city.name,
                        child: Text(city.name),
                      ),
                  ],
                  onChanged: state.saving
                      ? null
                      : (name) => _controller.selectCity(
                          Cities.all.firstWhere(
                            (c) => c.name == name,
                            orElse: () => state.city,
                          ),
                        ),
                  style: textTheme.bodyLarge,
                  dropdownColor: tokens.surface2,
                  decoration: InputDecoration(
                    errorText: state.fieldErrors['city'],
                  ),
                ),
                const SizedBox(height: AppSpace.s3),
                _Label('Address'),
                TextField(
                  controller: _address,
                  enabled: !state.saving,
                  textCapitalization: TextCapitalization.sentences,
                  textInputAction: TextInputAction.done,
                  minLines: 1,
                  maxLines: 2,
                  inputFormatters: [
                    LengthLimitingTextInputFormatter(
                      VenueDetailsController.addressMaxLength,
                    ),
                  ],
                  style: textTheme.bodyLarge,
                  decoration: InputDecoration(
                    hintText: 'e.g. 100 Ft Road, Indiranagar',
                    errorText: state.fieldErrors['addressLine'],
                  ),
                  onChanged: (_) => _controller.clearFieldError('addressLine'),
                ),
                const SizedBox(height: AppSpace.s3),
                _Label('Location pin'),
                SizedBox(
                  height: 180,
                  child: LocationPinMap(
                    // A new city starts a fresh map at its centre.
                    key: ValueKey(state.city.name),
                    initialCenter: state.pin ?? state.city.center,
                    zoomedIn: state.pin != null,
                    onMoved: _controller.placePin,
                    onTouch: (touching) =>
                        setState(() => _touchingMap = touching),
                  ),
                ),
                const SizedBox(height: AppSpace.s2),
                Text(
                  state.fieldErrors['location'] ??
                      (state.pin == null
                          ? 'Drag the map until the pin sits on your entrance.'
                          : 'Pin placed. Drag again to adjust.'),
                  style: textTheme.bodySmall?.copyWith(
                    color: state.fieldErrors.containsKey('location')
                        ? tokens.wineText
                        : tokens.textTertiary,
                  ),
                ),
                const SizedBox(height: AppSpace.s4),
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Icon(
                      Icons.lock_outline_rounded,
                      size: 18,
                      color: tokens.textTertiary,
                    ),
                    const SizedBox(width: AppSpace.s2 + 2),
                    Expanded(
                      child: Text(
                        'Drafts are private. Nothing is visible until you submit '
                        'and we verify it.',
                        style: textTheme.bodySmall?.copyWith(
                          color: tokens.textTertiary,
                        ),
                      ),
                    ),
                  ],
                ),
                if (state.error != null) ...[
                  const SizedBox(height: AppSpace.s4),
                  Text(
                    state.error!,
                    style: textTheme.bodyMedium?.copyWith(
                      color: tokens.wineText,
                    ),
                  ),
                ],
              ],
            ),
          ),
        ),
        DecoratedBox(
          decoration: BoxDecoration(
            border: Border(top: BorderSide(color: tokens.hair)),
          ),
          child: Padding(
            padding: const EdgeInsets.fromLTRB(
              AppSpace.s5,
              AppSpace.s4,
              AppSpace.s5,
              AppSpace.s4,
            ),
            child: PrimaryButton(
              label: 'Continue',
              loading: state.saving,
              onPressed: _save,
            ),
          ),
        ),
      ],
    );
  }
}

/// Field label with the design's amber "required" star.
class _Label extends StatelessWidget {
  const _Label(this.text);

  final String text;

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    final style = Theme.of(context).textTheme.labelMedium
        ?.copyWith(color: tokens.textSecondary);
    return Padding(
      padding: const EdgeInsets.only(bottom: AppSpace.s2 - 2),
      child: Text.rich(
        TextSpan(
          text: text,
          style: style,
          children: [
            TextSpan(
              text: ' *',
              style: style?.copyWith(color: tokens.amberText),
            ),
          ],
        ),
      ),
    );
  }
}

class _LoadFailed extends StatelessWidget {
  const _LoadFailed({
    required this.message,
    required this.onRetry,
    required this.onClose,
  });

  final String message;
  final VoidCallback onRetry;
  final VoidCallback onClose;

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    return Padding(
      padding: const EdgeInsets.all(AppSpace.s5),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(
            "Couldn't open your venue",
            style: textTheme.headlineMedium,
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: AppSpace.s2),
          Text(
            message,
            style: textTheme.bodyMedium?.copyWith(
              color: context.tokens.textSecondary,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: AppSpace.s6),
          PrimaryButton(label: 'Try again', onPressed: onRetry),
          TextButton(onPressed: onClose, child: const Text('Back')),
        ],
      ),
    );
  }
}
