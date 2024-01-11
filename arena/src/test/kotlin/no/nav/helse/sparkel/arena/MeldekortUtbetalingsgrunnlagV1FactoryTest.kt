package no.nav.helse.sparkel.arena

import org.junit.jupiter.api.Test

class MeldekortUtbetalingsgrunnlagV1FactoryTest {
    @Test
    fun `kan opprette objekt`() {
        val stsClientWs = stsClient(
            stsUrl = "http://sts",
            username = "demobruker",
            password = "testpassord"
        )

        org.junit.jupiter.api.assertDoesNotThrow {
            MeldekortUtbetalingsgrunnlagV1Factory.create("http://medlekort", stsClientWs)
        }
    }
}