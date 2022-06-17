CREATE TABLE aktor
(
    id           SERIAL PRIMARY KEY,
    idnummer     VARCHAR UNIQUE NOT NULL,
    type         VARCHAR NOT NULL,
    gjeldende    BOOLEAN NOT NULL,
    person_key   VARCHAR NOT NULL,
    melding_lest TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);