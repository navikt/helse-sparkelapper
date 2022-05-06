package no.nav.helse.sparkel.personinfo.leesah

import java.io.EOFException
import java.util.Base64
import java.util.UUID
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseFactory.adressebeskyttelseV1
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseFactory.serialize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.skyscreamer.jsonassert.JSONAssert

internal class PersonhendelseAvroDeserializerTest {

    @Test
    fun `klarer å parse personhendelsedokument fra leesah`() {
        val melding = adressebeskyttelseV1("20046913337")
        assertEquals(melding, serialize(melding).deserialiser())
    }

    @Test
    fun `klarer å parse personhendelsedokument fra leesah (base64)`() {
        val melding = adressebeskyttelseV1(fodselsnummer = "20046913337", hendelseId = UUID.fromString("c2e5e1c1-1c86-42cc-95ba-12c3880d4f3c"))
        val base64melding = "AAAAAABIYzJlNWUxYzEtMWM4Ni00MmNjLTk1YmEtMTJjMzg4MGQ0ZjNjAhYyMDA0NjkxMzMzNwAKc2thdHTIBipBRFJFU1NFQkVTS1lUVEVMU0VfVjECAAIGAAAAAAAAAAAAAAAAAAAAAA=="
        assertEquals(melding, serialize(melding).deserialiser())
        assertEquals(melding, Base64.getDecoder().decode(base64melding).deserialiser())
    }

    @Test
    fun `tom melding`() {
        assertThrows<EOFException> {
            ByteArray(0).deserialiser()
        }
    }

    @Test
    fun `ugyldig melding`() {
        assertThrows<EOFException> {
           """{"test": true}""".toByteArray().deserialiser()
        }
    }

    @Test
    fun `prototype av personhendelser er V11`() {
        val proto = PersonhendelseAvroDeserializerTest::class.java.getResource("/pdl/PersonhendelseProto.avsc")!!.readText()
        val v11 = PersonhendelseAvroDeserializerTest::class.java.getResource("/pdl/Personhendelse_V11.avsc")!!.readText()
        JSONAssert.assertEquals(proto, v11, true)
    }

    private companion object {
        private val deserializer = PersonhendelseAvroDeserializer()
        private fun ByteArray.deserialiser() = deserializer.deserialize("leesah", this)
    }
}
