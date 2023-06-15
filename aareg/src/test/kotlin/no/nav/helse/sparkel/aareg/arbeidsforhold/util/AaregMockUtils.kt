package no.nav.helse.sparkel.aareg.arbeidsforhold.util

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.mockk.every
import io.mockk.mockk
import org.intellij.lang.annotations.Language

val aaregmockGenerator = mockk<AaregResponseGenerator>(relaxed = true) {
    every { arbeidsforholdResponse() }.returns(defaultArbeidsforholdResponse())
}

interface AaregResponseGenerator {
    fun arbeidsforholdResponse(): String
}

fun aaregMockClient(aaregResponseGenerator: AaregResponseGenerator) = HttpClient(MockEngine) {
    engine {
        addHandler { request ->
            when {
                request.url.fullPath.startsWith("/v1/arbeidstaker/arbeidsforhold") -> {
                    respond(aaregResponseGenerator.arbeidsforholdResponse())
                }
                else -> error("Endepunktet finnes ikke ${request.url.fullPath}")
            }
        }
    }
}


@Language("JSON")
fun defaultArbeidsforholdResponse() = """[{
  "navArbeidsforholdId": 123456,
  "arbeidsforholdId": "abc-321",
  "arbeidstaker": {
    "type": "string",
    "offentligIdent": "31126700000",
    "aktoerId": "1234567890",
    "organisasjonsnummer": "987654321"
  },
  "arbeidsgiver": {
    "type": "string",
    "organisasjonsnummer": "987654321"
  },
  "opplysningspliktig": {
    "type": "string",
    "organisasjonsnummer": "987654321"
  },
  "type": "ordinaertArbeidsforhold",
  "ansettelsesperiode": {
    "periode": {
      "fom": "2014-07-01",
      "tom": "2015-12-31"
    },
    "sluttaarsak": "arbeidstakerHarSagtOppSelv",
    "varslingskode": "ERKONK",
    "bruksperiode": {
      "fom": "2015-01-06T21:44:04.748",
      "tom": "2015-12-06T19:45:04"
    },
    "sporingsinformasjon": {
      "opprettetTidspunkt": "2018-09-19T12:10:58.059",
      "opprettetAv": "srvappserver",
      "opprettetKilde": "EDAG",
      "opprettetKildereferanse": "22a26849-aeef-4b81-9174-e238c11e1081",
      "endretTidspunkt": "2018-09-19T12:11:20.79",
      "endretAv": "Z990693",
      "endretKilde": "AAREG",
      "endretKildereferanse": "referanse-fra-kilde"
    }
  },
  "arbeidsavtaler": [
    {
      "arbeidstidsordning": "ikkeSkift",
      "ansettelsesform": "fast",
      "yrke": "2130123",
      "stillingsprosent": 49.5,
      "antallTimerPrUke": 37.5,
      "beregnetAntallTimerPrUke": 37.5,
      "bruksperiode": {
        "fom": "2015-01-06T21:44:04.748",
        "tom": "2015-12-06T19:45:04"
      },
      "gyldighetsperiode": {
        "fom": "2014-07-01",
        "tom": "2015-12-31"
      },
      "sporingsinformasjon": {
        "opprettetTidspunkt": "2018-09-19T12:10:58.059",
        "opprettetAv": "srvappserver",
        "opprettetKilde": "EDAG",
        "opprettetKildereferanse": "22a26849-aeef-4b81-9174-e238c11e1081",
        "endretTidspunkt": "2018-09-19T12:11:20.79",
        "endretAv": "Z990693",
        "endretKilde": "AAREG",
        "endretKildereferanse": "referanse-fra-kilde"
      },
      "type": "Ordinaer,Maritim,Forenklet,Frilanser",
      "sistLoennsendring": "string",
      "sistStillingsendring": "string"
    }
  ],
  "permisjonPermitteringer": [
    {
      "permisjonPermitteringId": "123-xyz",
      "periode": {
        "fom": "2014-07-01",
        "tom": "2015-12-31"
      },
      "prosent": 50.5,
      "type": "permisjonMedForeldrepenger",
      "sporingsinformasjon": {
        "opprettetTidspunkt": "2018-09-19T12:10:58.059",
        "opprettetAv": "srvappserver",
        "opprettetKilde": "EDAG",
        "opprettetKildereferanse": "22a26849-aeef-4b81-9174-e238c11e1081",
        "endretTidspunkt": "2018-09-19T12:11:20.79",
        "endretAv": "Z990693",
        "endretKilde": "AAREG",
        "endretKildereferanse": "referanse-fra-kilde"
      },
      "varslingskode": "string"
    }
  ],
  "antallTimerForTimeloennet": [
    {
      "periode": {
        "fom": "2014-07-01",
        "tom": "2015-12-31"
      },
      "antallTimer": 37.5,
      "rapporteringsperiode": "2018-05",
      "sporingsinformasjon": {
        "opprettetTidspunkt": "2018-09-19T12:10:58.059",
        "opprettetAv": "srvappserver",
        "opprettetKilde": "EDAG",
        "opprettetKildereferanse": "22a26849-aeef-4b81-9174-e238c11e1081",
        "endretTidspunkt": "2018-09-19T12:11:20.79",
        "endretAv": "Z990693",
        "endretKilde": "AAREG",
        "endretKildereferanse": "referanse-fra-kilde"
      }
    }
  ],
  "utenlandsopphold": [
    {
      "periode": {
        "fom": "2014-07-01",
        "tom": "2015-12-31"
      },
      "landkode": "JPN",
      "rapporteringsperiode": "2017-12",
      "sporingsinformasjon": {
        "opprettetTidspunkt": "2018-09-19T12:10:58.059",
        "opprettetAv": "srvappserver",
        "opprettetKilde": "EDAG",
        "opprettetKildereferanse": "22a26849-aeef-4b81-9174-e238c11e1081",
        "endretTidspunkt": "2018-09-19T12:11:20.79",
        "endretAv": "Z990693",
        "endretKilde": "AAREG",
        "endretKildereferanse": "referanse-fra-kilde"
      }
    }
  ],
  "varsler": [
    {
      "entitet": "ARBEIDSFORHOLD",
      "varslingskode": "string"
    }
  ],
  "innrapportertEtterAOrdningen": true,
  "registrert": "2018-09-18T11:12:29",
  "sistBekreftet": "2018-09-19T12:10:31",
  "sporingsinformasjon": {
    "opprettetTidspunkt": "2018-09-19T12:10:58.059",
    "opprettetAv": "srvappserver",
    "opprettetKilde": "EDAG",
    "opprettetKildereferanse": "22a26849-aeef-4b81-9174-e238c11e1081",
    "endretTidspunkt": "2018-09-19T12:11:20.79",
    "endretAv": "Z990693",
    "endretKilde": "AAREG",
    "endretKildereferanse": "referanse-fra-kilde"
  }
}]
"""
