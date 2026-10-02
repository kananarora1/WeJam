-- Invited + accepted members, excluding the owner (who is always the group's admin).
-- Kept as a counter so the cap is enforced atomically (CLAUDE.md §5.4): 10 people max = owner + 9.
ALTER TABLE host_profiles
    ADD COLUMN member_count integer NOT NULL DEFAULT 0 CHECK (member_count BETWEEN 0 AND 9);

CREATE TABLE host_group_members (
    host_profile_id uuid        NOT NULL REFERENCES host_profiles (id) ON DELETE CASCADE,
    user_id         uuid        NOT NULL REFERENCES users (id),
    status          text        NOT NULL CHECK (status IN ('INVITED', 'ACCEPTED')),
    invited_at      timestamptz NOT NULL DEFAULT now(),
    accepted_at     timestamptz,
    PRIMARY KEY (host_profile_id, user_id),
    CHECK ((status = 'ACCEPTED') = (accepted_at IS NOT NULL))
);

-- "Invites to me" and "groups I'm in".
CREATE INDEX host_group_members_user_id_idx ON host_group_members (user_id);
