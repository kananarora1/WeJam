import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/api/api_error.dart';
import '../../../core/router/route_resolver.dart';
import '../../../core/theme/tokens.dart';
import '../../../core/widgets/eq_bars.dart';
import '../../../core/widgets/primary_button.dart';
import '../domain/venue.dart';
import 'my_venues_provider.dart';
import 'widgets/venue_status_badge.dart';

/// Home for venue owners until the venue tabs (Tonight, Requests, …) exist: their venues, with
/// what to do next for each.
class MyVenueScreen extends ConsumerWidget {
  const MyVenueScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tokens = context.tokens;
    final textTheme = Theme.of(context).textTheme;
    final venues = ref.watch(myVenuesProvider);

    return Scaffold(
      body: SafeArea(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(
                AppSpace.s5,
                AppSpace.s2,
                AppSpace.s3,
                0,
              ),
              child: Row(
                children: [
                  Text(
                    'VENUE',
                    style: textTheme.labelSmall?.copyWith(
                      color: tokens.textTertiary,
                    ),
                  ),
                  const Spacer(),
                  IconButton(
                    onPressed: () => context.push(Routes.profile),
                    tooltip: 'Profile',
                    icon: const Icon(Icons.person_outline_rounded),
                  ),
                ],
              ),
            ),
            Expanded(
              child: switch (venues) {
                AsyncValue(:final value?) when value.isEmpty => const _Empty(),
                AsyncValue(:final value?) => _VenueList(venues: value),
                AsyncValue(:final error?) => _Failed(
                  message: error is ApiFailure
                      ? error.message
                      : 'Something went wrong. Try again.',
                  onRetry: () => ref.invalidate(myVenuesProvider),
                ),
                _ => Center(child: EqBars(color: tokens.amber)),
              },
            ),
          ],
        ),
      ),
    );
  }
}

class _Empty extends StatelessWidget {
  const _Empty();

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    return Padding(
      padding: const EdgeInsets.all(AppSpace.s5),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const SizedBox(height: AppSpace.s6),
          Text('List your venue', style: textTheme.displayMedium),
          const SizedBox(height: AppSpace.s3),
          Text(
            'Four short steps: details, photos, your food license and (optionally) '
            'proof of space. It stays a private draft until you submit it for review.',
            style: textTheme.bodyLarge?.copyWith(
              color: context.tokens.textSecondary,
            ),
          ),
          const Spacer(),
          PrimaryButton(
            label: 'Start listing',
            onPressed: () => context.push(Routes.newVenue),
          ),
        ],
      ),
    );
  }
}

class _VenueList extends StatelessWidget {
  const _VenueList({required this.venues});

  final List<VenueSummary> venues;

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(
        AppSpace.s5,
        AppSpace.s3,
        AppSpace.s5,
        AppSpace.s5,
      ),
      children: [
        for (final venue in venues) ...[
          _VenueCard(venue: venue),
          const SizedBox(height: AppSpace.s3),
        ],
        TextButton(
          onPressed: () => context.push(Routes.newVenue),
          child: const Text('Add another venue'),
        ),
      ],
    );
  }
}

class _VenueCard extends StatelessWidget {
  const _VenueCard({required this.venue});

  final VenueSummary venue;

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    final textTheme = Theme.of(context).textTheme;
    final isDraft = venue.status == VenueStatus.draft;
    void openDetails() => context.push(Routes.venueDetails(venue.id));

    return Container(
      padding: const EdgeInsets.all(AppSpace.s4),
      decoration: BoxDecoration(
        color: tokens.surface1,
        borderRadius: BorderRadius.circular(AppRadii.lg),
        border: Border.all(color: tokens.hair),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          VenueStatusBadge(venue.status),
          const SizedBox(height: AppSpace.s3),
          Text(venue.name, style: textTheme.headlineMedium),
          const SizedBox(height: AppSpace.s1),
          Text(
            venue.city,
            style: textTheme.bodyMedium?.copyWith(color: tokens.textSecondary),
          ),
          const SizedBox(height: AppSpace.s4),
          if (isDraft) ...[
            Text(
              'Finish setting up to submit it for review.',
              style: textTheme.bodySmall?.copyWith(color: tokens.textTertiary),
            ),
            const SizedBox(height: AppSpace.s3),
            SizedBox(
              width: double.infinity,
              child: PrimaryButton(
                label: 'Continue setup',
                onPressed: openDetails,
              ),
            ),
          ] else
            SizedBox(
              width: double.infinity,
              child: OutlinedButton(
                onPressed: openDetails,
                child: const Text('Edit details'),
              ),
            ),
        ],
      ),
    );
  }
}

class _Failed extends StatelessWidget {
  const _Failed({required this.message, required this.onRetry});

  final String message;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(AppSpace.s5),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(
            message,
            style: Theme.of(context).textTheme.bodyLarge,
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: AppSpace.s5),
          PrimaryButton(label: 'Try again', onPressed: onRetry),
        ],
      ),
    );
  }
}
