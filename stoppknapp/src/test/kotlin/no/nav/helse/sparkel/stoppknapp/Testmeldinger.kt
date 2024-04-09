package no.nav.helse.sparkel.stoppknapp

import no.nav.helse.sparkel.stoppknapp.Testdata.FØDSELSNUMMER
import no.nav.helse.sparkel.stoppknapp.Testdata.STATUS
import no.nav.helse.sparkel.stoppknapp.Testdata.TIDSSTEMPEL
import no.nav.helse.sparkel.stoppknapp.Testdata.ÅRSAK
import org.intellij.lang.annotations.Language
import java.util.UUID

internal object Testmeldinger {
    @Language("JSON")
    internal fun stoppknappMelding(
        årsak: String = ÅRSAK,
        status: String = STATUS,
    ): String =
        """
        {
            "uuid": "${UUID.randomUUID()}",
            "veilederIdent": {
                "value": "TULLE_IDENT"
            },
            "sykmeldtFnr": {
                "value": "$FØDSELSNUMMER"
            },
            "status": "$status",
            "arsakList": [
                {
                    "type": "$årsak"
                }
            ],
            "virksomhetNr": {
                "value": "TULLE_VIRKSOMHET"
            },
            "opprettet": "$TIDSSTEMPEL",
            "enhetNr": {
                "value": "TULLE_ENHET"
            }
        }
        """.trimIndent()

    @Language("JSON")
    internal fun automatiseringStoppetAvVeilederBehov(): String =
        """
        {
            "@event_name": "behov",
            "@behov": [
                "AutomatiseringStoppetAvVeileder"
            ],
            "@id": "${UUID.randomUUID()}",
            "fødselsnummer": "$FØDSELSNUMMER"
        }
        """.trimIndent()
}
