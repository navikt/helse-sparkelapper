package no.nav.helse.sparkel.personinfo.leesah

import java.io.EOFException
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseFactory.nyttDokument
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseFactory.serialize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert

class PersonhendelseAvroDeserializerTest {
    @Test
    fun `klarer å parse personhendelsedokument fra leesah`() {
        val dokument = nyttDokument("20046913337")
        assertEquals(dokument, PersonhendelseAvroDeserializer().deserialize("leesah", serialize(dokument)))
    }

    @Test
    fun `tom melding`() {
        assertThrows<EOFException> {
            PersonhendelseAvroDeserializer().deserialize("leesah", ByteArray(0))
        }
    }

    @Test
    fun `ugyldig melding`() {
        assertThrows<EOFException> {
            PersonhendelseAvroDeserializer().deserialize("leesah", """{"test": true}""".toByteArray())
        }
    }

    @Test
    fun `prototype av personhendelser er V11`() {
        val proto = PersonhendelseAvroDeserializerTest::class.java.getResource("/pdl/PersonhendelseProto.avsc")!!.readText()
        val v11 = PersonhendelseAvroDeserializerTest::class.java.getResource("/pdl/Personhendelse_V11.avsc")!!.readText()
        JSONAssert.assertEquals(proto, v11, true)
    }
}
