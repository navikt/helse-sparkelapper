CREATE TABLE inntektsmelding_registrert
(
    id                 BIGSERIAL PRIMARY KEY,
    hendelse_id        UUID      NOT NULL,
    dokument_id        UUID      NOT NULL,
    opprettet          TIMESTAMP NOT NULL,
    CONSTRAINT unik_hendelse_og_dokument_constraint UNIQUE (hendelse_id, dokument_id)
);