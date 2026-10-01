ALTER TABLE venues
    -- The placeholder default only fills rows created before this migration (local dev data); it can never pass
    -- verification. Dropped below, so new venues must supply a real number.
    ADD COLUMN fssai_number        text NOT NULL DEFAULT '00000000000000' CHECK (fssai_number ~ '^[0-9]{14}$'),
    ADD COLUMN hosting_mode        text NOT NULL DEFAULT 'OPEN'
        CHECK (hosting_mode IN ('OPEN', 'SELF_ONLY')),
    ADD COLUMN sound_policy        text NOT NULL DEFAULT 'ACOUSTIC_ONLY'
        CHECK (sound_policy IN ('ACOUSTIC_ONLY', 'AMPLIFIED_ALLOWED')),
    -- Local time of day in time_zone; null = no curfew. Compared against event end times in step 6.
    ADD COLUMN sound_curfew        time,
    ADD COLUMN time_zone           text NOT NULL DEFAULT 'Asia/Kolkata',
    ADD COLUMN house_rules         text,
    ADD COLUMN verification_status text NOT NULL DEFAULT 'PENDING'
        CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    ADD COLUMN rejection_reason    text;

ALTER TABLE venues ALTER COLUMN fssai_number DROP DEFAULT;

-- Duplicates may wait as PENDING (so nobody can squat a café's license number by registering it first);
-- the platform admin decides, and the DB guarantees one license is verified for at most one venue.
CREATE UNIQUE INDEX venues_fssai_verified_uq ON venues (fssai_number) WHERE verification_status = 'VERIFIED';
