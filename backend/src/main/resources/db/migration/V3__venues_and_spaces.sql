CREATE TABLE venues (
    id           uuid             PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id     uuid             NOT NULL REFERENCES users (id),
    name         text             NOT NULL,
    description  text,
    address_line text             NOT NULL,
    city         text             NOT NULL,
    latitude     double precision NOT NULL CHECK (latitude BETWEEN -90 AND 90),
    longitude    double precision NOT NULL CHECK (longitude BETWEEN -180 AND 180),
    -- Derived by Postgres from lat/lng, so it can never disagree with them. Used by the nearby feed
    -- (step 5), which also adds its GiST index.
    location     geography(Point, 4326) GENERATED ALWAYS AS (
                     ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography
                 ) STORED,
    created_at   timestamptz      NOT NULL DEFAULT now(),
    updated_at   timestamptz      NOT NULL DEFAULT now()
);

-- GET /api/v1/me/venues
CREATE INDEX venues_owner_id_idx ON venues (owner_id);

CREATE TABLE spaces (
    id         uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    venue_id   uuid        NOT NULL REFERENCES venues (id) ON DELETE CASCADE,
    name       text        NOT NULL,
    capacity   integer     NOT NULL CHECK (capacity BETWEEN 1 AND 1000),
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);

-- Venue detail (spaces of a venue) and the ON DELETE CASCADE from venues.
CREATE INDEX spaces_venue_id_idx ON spaces (venue_id);

CREATE TABLE space_gear (
    space_id uuid    NOT NULL REFERENCES spaces (id) ON DELETE CASCADE,
    position integer NOT NULL,
    name     text    NOT NULL,
    details  text,
    PRIMARY KEY (space_id, position)
);
