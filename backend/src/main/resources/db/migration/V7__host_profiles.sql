CREATE TABLE host_profiles (
    id               uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    -- One host profile per user (individual or group) in v1.
    owner_id         uuid        NOT NULL UNIQUE REFERENCES users (id),
    type             text        NOT NULL CHECK (type IN ('INDIVIDUAL', 'GROUP')),
    group_kind       text        CHECK (group_kind IN ('BAND', 'FRIENDS', 'COMMUNITY')),
    group_name       text,
    bio              text        CHECK (char_length(bio) <= 200),
    area             text,
    instagram_handle text        CHECK (instagram_handle ~ '^[A-Za-z0-9._]{1,30}$'),
    created_at       timestamptz NOT NULL DEFAULT now(),
    updated_at       timestamptz NOT NULL DEFAULT now(),
    -- A group always has a kind and a name; an individual never does (its name is the owner's display name).
    CHECK ((type = 'GROUP') = (group_kind IS NOT NULL AND group_name IS NOT NULL)),
    CHECK (type = 'GROUP' OR (group_kind IS NULL AND group_name IS NULL))
);

CREATE TABLE host_profile_genres (
    host_profile_id uuid NOT NULL REFERENCES host_profiles (id) ON DELETE CASCADE,
    genre           text NOT NULL,
    PRIMARY KEY (host_profile_id, genre)
);

CREATE TABLE host_media_links (
    host_profile_id uuid    NOT NULL REFERENCES host_profiles (id) ON DELETE CASCADE,
    position        integer NOT NULL,
    url             text    NOT NULL CHECK (url ~ '^https?://'),
    title           text,
    PRIMARY KEY (host_profile_id, position)
);
