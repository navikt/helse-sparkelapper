# Personinfo

## Feil ved deserialisering av Personhendelse fra Leesah

Hva gjør jeg når tjenesten stopper og vi får warning i tjenestekall `Klarte ikke å deserialisere Personhendelse-melding fra Leesah*`?

Du kan gjenskap feilen med `Base64='<melding>'` meldingen fra logglinjen og kjøre den tilsvarende testen `klarer å parse personhendelsedokument fra leesah (base64)` i `PersonhendelseAvroDeserializer`

Hvordan oppdaterer jeg skjemaet til en ny versjon?

Legg til nyeste skjema i resources. Dette finner du i attributten `schema` på responsen på linkene under. Versjonen er i `version` (OBS! Lenkene nås ikke med naisdevice)

Dev: https://kafka-schema-registry.nais.preprod.local/subjects/aapen-person-pdl-leesah-v1-value/versions/latest

Prod: https://kafka-schema-registry.nais.adeo.no/subjects/aapen-person-pdl-leesah-v1-value/versions/latest

Behold de to siste versjonene av skjemaet og endre i `PersonhendelseAvroDeserializer`