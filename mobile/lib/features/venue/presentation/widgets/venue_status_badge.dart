import 'package:flutter/material.dart';

import '../../../../core/theme/tokens.dart';
import '../../domain/venue.dart';

/// Venue status chip (design badges): dashed for Draft / Pending review, amber rim for Verified,
/// wine tint for Not verified.
class VenueStatusBadge extends StatelessWidget {
  const VenueStatusBadge(this.status, {super.key, this.suffix});

  final VenueStatus status;

  /// Extra text after the label, e.g. "saved just now".
  final String? suffix;

  @override
  Widget build(BuildContext context) {
    final tokens = context.tokens;
    final (
      label,
      foreground,
      background,
      solidBorder,
      dashed,
    ) = switch (status) {
      VenueStatus.draft => ('Draft', tokens.textSecondary, null, null, true),
      VenueStatus.pending => (
        'Pending review',
        tokens.textSecondary,
        null,
        null,
        true,
      ),
      VenueStatus.verified => (
        'Verified',
        tokens.amberText,
        null,
        tokens.amber.withValues(alpha: 0.55),
        false,
      ),
      VenueStatus.rejected => (
        'Not verified',
        tokens.wineText,
        tokens.wine.withValues(alpha: 0.18),
        null,
        false,
      ),
      VenueStatus.unknown => (
        'Status unknown',
        tokens.textTertiary,
        null,
        null,
        true,
      ),
    };
    final text = suffix == null ? label : '$label · $suffix';

    final chip = Container(
      height: 24,
      padding: const EdgeInsets.symmetric(horizontal: AppSpace.s2 + 2),
      decoration: BoxDecoration(
        color: background,
        borderRadius: BorderRadius.circular(12),
        border: solidBorder == null ? null : Border.all(color: solidBorder),
      ),
      // widthFactor 1: as wide as the label, never the whole row.
      child: Center(
        widthFactor: 1,
        child: Text(
          text,
          style: Theme.of(context).textTheme.labelMedium
              ?.copyWith(fontSize: 12, color: foreground),
        ),
      ),
    );
    return dashed
        ? CustomPaint(painter: _DashedBorderPainter(tokens.line), child: chip)
        : chip;
  }
}

class _DashedBorderPainter extends CustomPainter {
  const _DashedBorderPainter(this.color);

  final Color color;

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1;
    final outline = Path()
      ..addRRect(
        RRect.fromRectAndRadius(
          Offset.zero & size,
          Radius.circular(size.height / 2),
        ).deflate(0.5),
      );
    for (final metric in outline.computeMetrics()) {
      for (var d = 0.0; d < metric.length; d += 6) {
        canvas.drawPath(metric.extractPath(d, d + 3), paint);
      }
    }
  }

  @override
  bool shouldRepaint(_DashedBorderPainter old) => old.color != color;
}
