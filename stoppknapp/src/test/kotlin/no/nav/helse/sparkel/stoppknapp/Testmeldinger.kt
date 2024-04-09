package no.nav.helse.sparkel.stoppknapp

import no.nav.helse.sparkel.stoppknapp.Testdata.FØDSELSNUMMER
import no.nav.helse.sparkel.stoppknapp.Testdata.STATUS
import no.nav.helse.sparkel.stoppknapp.Testdata.TIDSSTEMPEL
import no.nav.helse.sparkel.stoppknapp.Testdata.ÅRSAK
import org.intellij.lang.annotations.Language
import java.util.UUID

internal object Testmeldinger {
    @Language("JSON")
    internal fun stoppknappMelding(): String =
        """
        {
          "uuid": "${UUID.randomUUID()}",
          "veilederIdent": {
            "value": "TULLE_IDENT"
          },
          "sykmeldtFnr": {
            "value": "$FØDSELSNUMMER"
          },
          "status": "$STATUS",
          "arsakList": [{
            "type": "$ÅRSAK"
          }],
          "virksomhetNr": {
            "value": "TULLE_VIRKSOMHET"
          },
          "opprettet": "$TIDSSTEMPEL",
          "enhetNr": {
            "value": "TULLE_ENHET"
          }
        }
        """.trimIndent()
}
