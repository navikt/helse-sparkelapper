package no.nav.helse.sparkel.personinfo.leesah

import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseFactory.nyttDokument
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseFactory.serialize
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.EncoderFactory
import org.apache.commons.codec.binary.Base64
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class PersonhendelseAvroDeserializerTest {
    @Test
    fun `klarer å parse personhendelsedokument fra leesah`() {
        val dokument = nyttDokument("20046913337")
        assertEquals(dokument, PersonhendelseAvroDeserializer().deserialize("leesah", serialize(dokument)))
    }
}
