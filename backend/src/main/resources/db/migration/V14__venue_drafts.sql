-- Venues start as private drafts that save on every onboarding step (design 7.1) and enter review only when the
-- owner submits (7.3 "Submit for review"). License details may be missing while drafting, never afterwards.
ALTER TABLE venues DROP CONSTRAINT venues_verification_status_check;
ALTER TABLE venues ADD CONSTRAINT venues_verification_status_check
    CHECK (verification_status IN ('DRAFT', 'PENDING', 'VERIFIED', 'REJECTED'));
ALTER TABLE venues ALTER COLUMN verification_status SET DEFAULT 'DRAFT';
ALTER TABLE venues ALTER COLUMN fssai_number DROP NOT NULL;

-- Dev data from before step 5d has no license details; it goes back to being a draft (no production data exists).
UPDATE venues SET verification_status = 'DRAFT', rejection_reason = NULL, verification_issues = '{}'
WHERE fssai_license_name IS NULL;

ALTER TABLE venues ADD CONSTRAINT venues_submitted_license_check CHECK (
    verification_status = 'DRAFT'
    OR (fssai_number IS NOT NULL AND fssai_license_name IS NOT NULL AND fssai_valid_until IS NOT NULL));
