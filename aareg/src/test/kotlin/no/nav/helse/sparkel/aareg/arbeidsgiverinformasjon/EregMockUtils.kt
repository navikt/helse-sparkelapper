package no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.mockk.every
import io.mockk.mockk
import org.intellij.lang.annotations.Language

val mockGenerator = mockk<ResponseGenerator>(relaxed = true) {
    val defaultOrganisasjonResponse = defaultOrganisasjonResponse()
    every { organisasjonResponse() }.returns(defaultOrganisasjonResponse)
}

interface ResponseGenerator {
    fun organisasjonResponse():String
}

fun eregMockClient(responseGenerator: ResponseGenerator) = HttpClient(MockEngine) {
    engine {
        addHandler { request ->
            when {
                request.url.fullPath.startsWith("/api/v1/organisasjon") -> {
                    respond(responseGenerator.organisasjonResponse())
                }
                else -> error("Endepunktet finnes ikke ${request.url.fullPath}")
            }
        }
    }
}


@Language("JSON")
fun defaultOrganisasjonResponse() = """{
    "navn": {
        "bruksperiode": {
            "fom": "2015-01-06T21:44:04.748",
            "tom": "2015-12-06T19:45:04"
        },
        "gyldighetsperiode": {
            "fom": "2014-07-01",
            "tom": "2015-12-31"
        },
        "navnelinje1": "NAV FAMILIE- OG PENSJONSYTELSER",
        "navnelinje2": null,
        "navnelinje4": "navn - linje 4",
        "navnelinje5": "siste navn",
        "redigertnavn": "NAV FAMILIE- OG PENSJONSYTELSER OSL"
    },
    "organisasjonDetaljer": {
        "ansatte": [
            {
                "antall": 123,
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                }
            }
        ],
        "dubletter": [
            null
        ],
        "enhetstyper": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "enhetstype": "BEDR",
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                }
            }
        ],
        "epostadresser": [
            {
                "adresse": "post@organisasjon.no",
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                }
            }
        ],
        "formaal": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "formaal": "Veivedlikehold, vaktmestertjenester, transport.",
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                }
            }
        ],
        "forretningsadresser": [
            {
                "adresselinje1": "string",
                "adresselinje2": "string",
                "adresselinje3": "string",
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "kommunenummer": "0301",
                "landkode": "JPN",
                "postnummer": "0557",
                "poststed": "string"
            }
        ],
        "hjemlandregistre": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "navn1": "string",
                "navn2": "string",
                "navn3": "string",
                "postadresse": {
                    "adresselinje1": "string",
                    "adresselinje2": "string",
                    "adresselinje3": "string",
                    "bruksperiode": {
                        "fom": "2015-01-06T21:44:04.748",
                        "tom": "2015-12-06T19:45:04"
                    },
                    "gyldighetsperiode": {
                        "fom": "2014-07-01",
                        "tom": "2015-12-31"
                    },
                    "kommunenummer": "0301",
                    "landkode": "JPN",
                    "postnummer": "0557",
                    "poststed": "string"
                },
                "registernummer": "0932568"
            }
        ],
        "internettadresser": [
            {
                "adresse": "www.nav.no",
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                }
            }
        ],
        "maalform": "NB",
        "mobiltelefonnummer": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "nummer": "21 07 00 00",
                "telefontype": "TFON"
            }
        ],
        "naeringer": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "hjelpeenhet": false,
                "naeringskode": 62.03
            }
        ],
        "navSpesifikkInformasjon": {
            "bruksperiode": {
                "fom": "2015-01-06T21:44:04.748",
                "tom": "2015-12-06T19:45:04"
            },
            "erIA": true,
            "gyldighetsperiode": {
                "fom": "2014-07-01",
                "tom": "2015-12-31"
            }
        },
        "navn": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "navnelinje1": "NAV FAMILIE- OG PENSJONSYTELSER",
                "navnelinje2": "string",
                "navnelinje3": "string",
                "navnelinje4": "string",
                "navnelinje5": "string",
                "redigertnavn": "NAV FAMILIE- OG PENSJONSYTELSER OSL"
            }
        ],
        "opphoersdato": "2016-12-31",
        "postadresser": [
            {
                "adresselinje1": "string",
                "adresselinje2": "string",
                "adresselinje3": "string",
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "kommunenummer": "0301",
                "landkode": "JPN",
                "postnummer": "0557",
                "poststed": "string"
            }
        ],
        "registreringsdato": "2014-07-15T12:10:58.059",
        "registrertMVA": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "registrertIMVA": true
            }
        ],
        "sistEndret": "2015-01-13",
        "statuser": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "kode": "KONK"
            }
        ],
        "stiftelsesdato": "2014-07-15",
        "telefaksnummer": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "nummer": "21 07 00 00",
                "telefontype": "TFON"
            }
        ],
        "telefonnummer": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "nummer": "21 07 00 00",
                "telefontype": "TFON"
            }
        ],
        "underlagtHjemlandLovgivningForetaksform": [
            {
                "beskrivelseHjemland": "string",
                "beskrivelseNorge": "string",
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "foretaksform": "LTD",
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "landkode": "GB"
            }
        ]
    },
    "organisasjonsnummer": 990983666,
    "type": "Virksomhet"
}
"""

@Language("JSON")
fun organisasjonUtenNæringerResponse() = """{
    "navn": {
        "bruksperiode": {
            "fom": "2015-01-06T21:44:04.748",
            "tom": "2015-12-06T19:45:04"
        },
        "gyldighetsperiode": {
            "fom": "2014-07-01",
            "tom": "2015-12-31"
        },
        "navnelinje1": "NAV FAMILIE- OG PENSJONSYTELSER",
        "navnelinje2": null,
        "navnelinje4": "navn - linje 4",
        "navnelinje5": "siste navn",
        "redigertnavn": "NAV FAMILIE- OG PENSJONSYTELSER OSL"
    },
    "organisasjonDetaljer": {
        "ansatte": [
            {
                "antall": 123,
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                }
            }
        ],
        "dubletter": [
            null
        ],
        "enhetstyper": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "enhetstype": "BEDR",
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                }
            }
        ],
        "epostadresser": [
            {
                "adresse": "post@organisasjon.no",
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                }
            }
        ],
        "formaal": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "formaal": "Veivedlikehold, vaktmestertjenester, transport.",
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                }
            }
        ],
        "forretningsadresser": [
            {
                "adresselinje1": "string",
                "adresselinje2": "string",
                "adresselinje3": "string",
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "kommunenummer": "0301",
                "landkode": "JPN",
                "postnummer": "0557",
                "poststed": "string"
            }
        ],
        "hjemlandregistre": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "navn1": "string",
                "navn2": "string",
                "navn3": "string",
                "postadresse": {
                    "adresselinje1": "string",
                    "adresselinje2": "string",
                    "adresselinje3": "string",
                    "bruksperiode": {
                        "fom": "2015-01-06T21:44:04.748",
                        "tom": "2015-12-06T19:45:04"
                    },
                    "gyldighetsperiode": {
                        "fom": "2014-07-01",
                        "tom": "2015-12-31"
                    },
                    "kommunenummer": "0301",
                    "landkode": "JPN",
                    "postnummer": "0557",
                    "poststed": "string"
                },
                "registernummer": "0932568"
            }
        ],
        "internettadresser": [
            {
                "adresse": "www.nav.no",
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                }
            }
        ],
        "maalform": "NB",
        "mobiltelefonnummer": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "nummer": "21 07 00 00",
                "telefontype": "TFON"
            }
        ],
        "navSpesifikkInformasjon": {
            "bruksperiode": {
                "fom": "2015-01-06T21:44:04.748",
                "tom": "2015-12-06T19:45:04"
            },
            "erIA": true,
            "gyldighetsperiode": {
                "fom": "2014-07-01",
                "tom": "2015-12-31"
            }
        },
        "navn": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "navnelinje1": "NAV FAMILIE- OG PENSJONSYTELSER",
                "navnelinje2": "string",
                "navnelinje3": "string",
                "navnelinje4": "string",
                "navnelinje5": "string",
                "redigertnavn": "NAV FAMILIE- OG PENSJONSYTELSER OSL"
            }
        ],
        "opphoersdato": "2016-12-31",
        "postadresser": [
            {
                "adresselinje1": "string",
                "adresselinje2": "string",
                "adresselinje3": "string",
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "kommunenummer": "0301",
                "landkode": "JPN",
                "postnummer": "0557",
                "poststed": "string"
            }
        ],
        "registreringsdato": "2014-07-15T12:10:58.059",
        "registrertMVA": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "registrertIMVA": true
            }
        ],
        "sistEndret": "2015-01-13",
        "statuser": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "kode": "KONK"
            }
        ],
        "stiftelsesdato": "2014-07-15",
        "telefaksnummer": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "nummer": "21 07 00 00",
                "telefontype": "TFON"
            }
        ],
        "telefonnummer": [
            {
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "nummer": "21 07 00 00",
                "telefontype": "TFON"
            }
        ],
        "underlagtHjemlandLovgivningForetaksform": [
            {
                "beskrivelseHjemland": "string",
                "beskrivelseNorge": "string",
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "foretaksform": "LTD",
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "landkode": "GB"
            }
        ]
    },
    "organisasjonsnummer": 990983666,
    "type": "Virksomhet"
}
"""
@Language("JSON")
fun organisasjonMedJuridiskEnhetResponse() = """
    {
    "organisasjonsnummer": "orgnummer",
    "type": "Virksomhet",
    "navn": {
        "navnelinje1": "navnelinje",
        "bruksperiode": {
            "fom": "2015-03-23T09:04:53.2"
        },
        "gyldighetsperiode": {
            "fom": "2000-09-11"
        }
    },
    "organisasjonDetaljer": {
        "registreringsdato": "2000-09-11T00:00:00",
        "enhetstyper": [
            {
                "enhetstype": "BEDR",
                "bruksperiode": {
                    "fom": "2015-05-21T15:30:46.546"
                },
                "gyldighetsperiode": {
                    "fom": "2000-09-11"
                }
            }
        ]
    },
    "virksomhetDetaljer": {
        "enhetstype": "BEDR",
        "oppstartsdato": "2000-08-01"
    },
    "inngaarIJuridiskEnheter": [
        {
            "organisasjonsnummer": "orgnummerJuridiskEnhet",
            "navn": {
                "navnelinje1": "navnOgnummerJuridisk",
                "bruksperiode": {
                    "fom": "2015-03-23T08:04:53.2"
                },
                "gyldighetsperiode": {
                    "fom": "2000-08-10"
                }
            },
            "bruksperiode": {
                "fom": "2014-03-23T18:45:53.408"
            },
            "gyldighetsperiode": {
                "fom": "2000-09-11"
            }
        }
    ]
}
"""