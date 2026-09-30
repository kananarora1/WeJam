import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:wejam/core/theme/app_theme.dart';
import 'package:wejam/features/auth/presentation/widgets/otp_input.dart';

void main() {
  Future<List<String>> pumpOtp(
    WidgetTester tester,
    TextEditingController controller,
  ) async {
    final completed = <String>[];
    await tester.pumpWidget(
      MaterialApp(
        theme: buildDarkTheme(),
        home: Scaffold(
          body: OtpInput(controller: controller, onCompleted: completed.add),
        ),
      ),
    );
    return completed;
  }

  testWidgets('digits fill the boxes left to right', (tester) async {
    final controller = TextEditingController();
    final completed = await pumpOtp(tester, controller);

    await tester.enterText(find.byKey(const Key('otp-field')), '47');
    await tester.pump();

    expect(find.text('4'), findsOneWidget);
    expect(find.text('7'), findsOneWidget);
    expect(completed, isEmpty);
  });

  testWidgets('the sixth digit auto-submits the whole code once', (
    tester,
  ) async {
    final controller = TextEditingController();
    final completed = await pumpOtp(tester, controller);

    await tester.enterText(find.byKey(const Key('otp-field')), '470213');
    await tester.pump();

    expect(completed, ['470213']);
  });

  testWidgets('non-digits and extra digits are dropped', (tester) async {
    final controller = TextEditingController();
    await pumpOtp(tester, controller);

    await tester.enterText(find.byKey(const Key('otp-field')), '12a3456789');
    await tester.pump();

    expect(controller.text, '123456');
  });
}
