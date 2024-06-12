# Personinfo

Jeg er en app som snakker med PDL for å hente ut personinfo. Jeg er blant annet interessert i dødsinfo, historiske identer og hente ut personinfo for visning i saksbehandlingsflaten.

## Feil ved deserialisering av Personhendelse fra Leesah

Hva gjør jeg når tjenesten stopper og vi får warning i tjenestekall `Klarte ikke å deserialisere Personhendelse-melding fra Leesah*`?

Du kan gjenskap feilen med `Base64='<melding>'` meldingen fra logglinjen og kjøre den tilsvarende testen `klarer å parse personhendelsedokument fra leesah (base64)` i `PersonhendelseAvroDeserializer`

Hvordan oppdaterer jeg skjemaet til en ny versjon?

Legg til nyeste skjema i resources. Dette finner du i attributten `schema` på responsen på linken under.

Man kan hente skjema på flere måter, her er tre:
1. Bruk skriptet `hent_pdl_leesah_schema.sh`, se videre instruksjoner i fila
2. Fra lokal maskin med naisdevice tilkoblet:
   1. Stjel URL og credentials fra en aiven-secret
   2. Kjør kommando:
      - `curl -v -u <username>:<password> https://nav-dev-kafka-nav-dev.aivencloud.com:26487/subjects/pdl.leesah-v1-value/versions/<version>`
3. `exec` inn i en pod som har shell (altså ikke distroless) og bruker kafka, og kjør:
   - `curl -u $KAFKA_SCHEMA_REGISTRY_USER:$KAFKA_SCHEMA_REGISTRY_PASSWORD $KAFKA_SCHEMA_REGISTRY/subjects/pdl.leesah-v1-value/versions/<versjon>`

Behold de to siste versjonene av skjemaet og endre i `PersonhendelseAvroDeserializer`
