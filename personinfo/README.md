# Personinfo

Jeg er en app som snakker med PDL for å hente ut personinfo. Jeg er blant annet interessert i dødsinfo, historiske identer og hente ut personinfo for visning i saksbehandlingsflaten.

## Feil ved deserialisering av Personhendelse fra Leesah

Hva gjør jeg når tjenesten stopper og vi får warning i tjenestekall `Klarte ikke å deserialisere Personhendelse-melding fra Leesah*`?

Du kan gjenskap feilen med `Base64='<melding>'` meldingen fra logglinjen og kjøre den tilsvarende testen `klarer å parse personhendelsedokument fra leesah (base64)` i `PersonhendelseAvroDeserializer`

Hvordan oppdaterer jeg skjemaet til en ny versjon?

Legg til nyeste skjema i resources. Dette finner du i attributten `schema` på responsen på linken under. Versjonen er i `version`.

Lenkene nås ikke med naisdevice, du må `exec`-e inn i en pod som bruker kafka. Kjør fra en pod i dev eller prod, alt ettersom.

```shell
curl -u $KAFKA_SCHEMA_REGISTRY_USER:$KAFKA_SCHEMA_REGISTRY_PASSWORD $KAFKA_SCHEMA_REGISTRY/subjects/pdl.leesah-v1-value/versions[/<versjon]
```

Behold de to siste versjonene av skjemaet og endre i `PersonhendelseAvroDeserializer`
