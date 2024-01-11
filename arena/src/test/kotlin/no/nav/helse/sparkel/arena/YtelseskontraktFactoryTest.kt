package no.nav.helse.sparkel.arena

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class YtelseskontraktFactoryTest {

    @Test
    fun `kan opprette objekt`() {
        val stsClientWs = stsClient(
            stsUrl = "http://sts",
            username = "demobruker",
            password = "testpassord"
        )

        assertDoesNotThrow {
            YtelseskontraktFactory.create("http://arena", stsClientWs)
        }
    }
}