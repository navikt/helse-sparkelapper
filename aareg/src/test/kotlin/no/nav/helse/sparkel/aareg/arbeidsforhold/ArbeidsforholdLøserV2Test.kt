package no.nav.helse.sparkel.aareg.arbeidsforhold

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.UUID
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.aareg.arbeidsforhold.util.aaregMockClient
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ArbeidsforholdLøserV2Test {
    private val objectMapper = jacksonObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(JavaTimeModule())

    private lateinit var sendtMelding: JsonNode

    private val rapid = object : RapidsConnection() {
        fun sendTestMessage(message: String) {
            notifyMessage(message, this)
        }

        override fun publish(message: String) { sendtMelding = objectMapper.readTree(message) }
        override fun rapidName(): String {
            return "Test"
        }

        override fun publish(key: String, message: String) {}

        override fun start() {}

        override fun stop() {}
    }

    @Test
    internal fun `løser ArbeidsforholdV2`() {
        val behov = """{"@id": "${UUID.randomUUID()}", "@behov":["ArbeidsforholdV2"], "fødselsnummer": "fnr", "vedtaksperiodeId": "id" }"""
        val mockAaregClient = AaregClient(
            baseUrl = "http://baseUrl.local",
            tokenSupplier = { "superToken" },
            httpClient = aaregMockClient()
        )
        ArbeidsforholdLøserV2(rapid, mockAaregClient)
        rapid.sendTestMessage(behov)
        val løsning = sendtMelding.løsning("ArbeidsforholdV2")
        assertTrue(løsning.isNotEmpty())
    }

    @Test
    internal fun `løser også AlleArbeidsforhold`() {
        val behov = """{"@id": "${UUID.randomUUID()}", "@behov":["AlleArbeidsforhold"], "fødselsnummer": "fnr", "vedtaksperiodeId": "id" }"""
        val mockAaregClient = AaregClient(
            baseUrl = "http://baseUrl.local",
            tokenSupplier = { "superToken" },
            httpClient = aaregMockClient()
        )
        ArbeidsforholdLøserV2(rapid, mockAaregClient)
        rapid.sendTestMessage(behov)
        val løsning = sendtMelding.løsning("AlleArbeidsforhold")
        assertTrue(løsning.isNotEmpty())
    }

    private fun JsonNode.løsning(behov: String): List<Arbeidsforhold> =
        this.path("@løsning")
            .path(behov)
            .map {
                Arbeidsforhold(
                    it["orgnummer"].asText(),
                    it["ansattSiden"].asLocalDate(),
                    it["ansattTil"].asOptionalLocalDate(),
                    Arbeidsforholdtype.valueOf(it["type"].asText())
                )
            }
}
