## Putte data i mocken

### Historikk som vises i Speil
1. Sett opp port-forwarding til pod-en, eksempelvis `k port-forward sparkel-sykepengeperioder-mock-94686b4fc-nsdp7 8080:8080`
2. Legg inn data i en fil som du gir et navn. Eksempel på innhold:
   1.  ```
       [
            {
              "fom": "2023-09-17",
              "tom": "2023-09-30",
              "dagsats": 1731.0,
              "grad": "100",
              "typetekst": "Utbetaling",
              "organisasjonsnummer": "947064649"
            }
       ]
       ```
3. Bruk curl for å putte dataene inn i mocken: `curl -v localhost:8080/utbetalingshistorikk/01927699342 \
   -H "Content-Type: application/json" \
   --data-binary "@<putt inn filnavn her>"`
4. Hvis du ser "200 OK" i outputen gikk det bra, og du kan for eksempel oppdatere persondata fra Speil for å se resultatet.

Hvis du vil fjerne historikken kan du kjøre `curl -v localhost:8080/reset -X POST` - fjerner alt fra mocken.
