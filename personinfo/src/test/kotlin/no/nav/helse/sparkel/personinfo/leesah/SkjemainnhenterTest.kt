package no.nav.helse.sparkel.personinfo.leesah

import no.nav.helse.sparkel.personinfo.leesah.Skjemainnhenter.resourceUrl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SkjemainnhenterTest {

    @Test
    fun `parse response fra schema registry`() {
        val fraSchemaRegistry = Skjemainnhenter.hentSkjema("/pdl/schema-registry-response-V12.json".resourceUrl())
        val fraResources = Skjemainnhenter.hentSkjema("/pdl/Personhendelse_V12.avsc".resourceUrl()) { "12" }
        assertEquals(fraSchemaRegistry, fraResources)
    }
}