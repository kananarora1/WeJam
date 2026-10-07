-- A draft hasn't been sent for review, so it has no "requested at" (V14 left the old NOT NULL in place, which
-- rejected every new draft). Submitting, resubmitting or a license change sets it; the admin queue orders by it.
ALTER TABLE venues ALTER COLUMN verification_requested_at DROP NOT NULL;
ALTER TABLE venues ALTER COLUMN verification_requested_at DROP DEFAULT;
UPDATE venues SET verification_requested_at = NULL WHERE verification_status = 'DRAFT';
ALTER TABLE venues ADD CONSTRAINT venues_verification_requested_at_check
    CHECK ((verification_status = 'DRAFT') = (verification_requested_at IS NULL));
