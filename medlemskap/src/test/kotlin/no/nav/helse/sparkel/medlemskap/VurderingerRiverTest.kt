package no.nav.helse.sparkel.medlemskap

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import java.util.UUID
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class VurderingerRiverTest {

    private val rapid: TestRapid = TestRapid().apply(::VurderingerRiver)

    private fun sendEvent(behov: String) = rapid.sendTestMessage(behov)

    @Test
    fun `vurderer fra melding`() {
        val melding = sendEvent(Meldinger.Ok)
    }

    object Meldinger {
        @Language("JSON")
        internal val Ok = """
            {
                "soknadId": "${UUID.randomUUID()}",
                "speilSvar": "JA",
                "fnr": "12345678910"
            }
        """.trimIndent()
    }

}
