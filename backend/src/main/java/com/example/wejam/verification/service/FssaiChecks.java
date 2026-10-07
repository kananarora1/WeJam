package com.example.wejam.verification.service;

/**
 * Offline heuristics only — the real FSSAI registry isn't called (CLAUDE.md §1: verification APIs are
 * mocked/manual). Layout: [licence type 1|2][state code 01–38][year][office][serial], 14 digits.
 */
import java.time.LocalDate;

final class FssaiChecks {

    private FssaiChecks() {
    }

    static boolean structureLooksValid(String fssai) {
        if (fssai == null || !fssai.matches("\\d{14}")) {
            return false;
        }
        char type = fssai.charAt(0);
        int stateCode = Integer.parseInt(fssai.substring(1, 3));
        return (type == '1' || type == '2') && stateCode >= 1 && stateCode <= 38;
    }

    /** Valid through the whole of its last day. Unknown (older venues) isn't reported as expired. */
    static boolean expired(LocalDate validUntil, LocalDate today) {
        return validUntil != null && validUntil.isBefore(today);
    }
}
