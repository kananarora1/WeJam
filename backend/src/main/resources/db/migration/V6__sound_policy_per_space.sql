-- Sound policy, curfew and house rules belong to a space, not the whole venue (design 8.4): a rooftop can be
-- acoustic-only while the main floor allows amplified sound. Existing values are copied to every space of the venue.
ALTER TABLE spaces
    ADD COLUMN sound_policy text NOT NULL DEFAULT 'ACOUSTIC_ONLY'
        CHECK (sound_policy IN ('ACOUSTIC_ONLY', 'AMPLIFIED_ALLOWED')),
    -- Local time of day in the venue's time_zone; null = no curfew.
    ADD COLUMN sound_curfew time,
    ADD COLUMN house_rules  text;

UPDATE spaces s
SET sound_policy = v.sound_policy,
    sound_curfew = v.sound_curfew,
    house_rules  = v.house_rules
FROM venues v
WHERE s.venue_id = v.id;

ALTER TABLE spaces ALTER COLUMN sound_policy DROP DEFAULT;

ALTER TABLE venues
    DROP COLUMN sound_policy,
    DROP COLUMN sound_curfew,
    DROP COLUMN house_rules;
