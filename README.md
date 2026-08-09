# Sparkel

Mikrotjenester som svarer ut behov ved å hente data fra ulike registre.

Dette er for øvrig et skikkelig bra sted å skrive navnene på hver tjeneste og en one-liner om hva de er ment å gjøre. Og så kan man bruke README for hver tjeneste for å beskrive nøyere.

## Legge til en ny gradle-modul

1. Lag en mappe og sørg for at det finnes en `build.gradle.kts` der, som tar i bruk
   `no.nav.helse.sas.sas-kotlin` (bibliotek) eller `no.nav.helse.sas.sas-deployable` (app)
2. Legg modulen inn i `include(...)` i `settings.gradle.kts`
3. Lag App.kt i `src/main/kotlin/no/nav/helse/sparkel/[app]`

## Legge til ny app

Hver app har sin egen `main-[app].yml`-workflow som bygger, tester, lager image med jib og
deployer. Navnet på appen blir prefikset med `sparkel-` i `config/nais.yml`, så navnet på
modulen skal være uten.

1. Gjør 'Legge til en ny gradle-modul', med `no.nav.helse.sas.sas-deployable`.
   Mappenavnet korresponderer med appnavnet.
2. Sett `mainClass` og `imageName = "helse-sparkelapper-[app]"` i `sasDeployable`-blokka.
3. Lag `config/[app]/[cluster].yml` for de klustrene appen skal deployes til.
   Fila må inneholde `app: [app]`, siden `config/nais.yml` er en delt mal.
4. Lag `.github/workflows/main-[app].yml` etter mønster fra en eksisterende app, og legg
   den nye appen inn i `paths`-ekskluderingene i de andre `main-*.yml`-workflowene.
5. Push endringene

## Oppgradering av gradle wrapper

Finn nyeste versjon av gradle her: https://gradle.org/releases/

Kjør denne kommandoen for å oppdatere `gradle-wrapper.properties`:

`./gradlew wrapper --gradle-version <versjon>`

Kjør samme kommando en gang til for å laste ned eventuelt oppdaterte jar- og skript-filer.

## Hvorfor Sparkel?

Det ble tidlig klart at sykepenge-tjenesten ville måtte hente data fra en del forskjellige tjenester utenfor teamet (og etter hvert som Produktområde Helse ble grunnlagt, uten POet). Det ble også klart at det var svært mange forskjellige måter å hente denne dataen på. Noen tjenester tilbød http-apier, noen brukte GraphQL, noen brukte ActiveMQ, osv. Vi ønsket å slippe at våre kjerneapplikasjoner skulle måtte forholde seg til alle disse forskjellige protokollene, så vi laget et sett med mikrotjenester, der hver tjeneste skulle integrere mot én ekstern tjeneste hver. Dermed fikk hver mikrotjeneste én protokoll å forholde seg til, og kjerneapplikasjonene kunne bruke samme måte å hente data på uavhengig av kilde.

Vi tenkte at en slik harmonisering av grensesnitt fungerte som et lag sparkel over en ujevn vegg, så da var fellesnavnet for disse applikasjonene selvsagt: Sparkel.

## Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

### For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen [#team-sas-værsågod](https://nav-it.slack.com/archives/C019637N90X)
