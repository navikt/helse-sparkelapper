package no.nav.helse.sparkel.medlemskap

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.lang.IllegalArgumentException
import java.net.SocketTimeoutException
import java.time.LocalDate.now
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class WorkaroundHandlerTest {

    @Test
    fun `Read timed out`() {
        val request: () -> Pair<Int, String?> = { throw SocketTimeoutException("Read timed out") }
        val workaroundHandler = WorkaroundHandler()
        // Feiler første gang
        assertThrows<SocketTimeoutException> {
            workaroundHandler.handle("fnr", now(), now()) { request() }
        }
        // Går bra andre gang
        val (responseCode, responseBody) = workaroundHandler.handle("fnr", now(), now()) { request() }
        assertEquals(200, responseCode)
        assertEquals("VetIkke", objectMapper.readTree(responseBody).path("resultat").path("svar").asText())

        // Feiler igjen neste gang
        assertThrows<SocketTimeoutException> {
            workaroundHandler.handle("fnr", now(), now()) { request() }
        }
    }

    @Test
    fun `Annen SocketTimeoutException`() {
        val request: () -> Pair<Int, String?> = { throw SocketTimeoutException()}
        val workaroundHandler = WorkaroundHandler()
        (1..3).forEach { _ ->
            assertThrows<SocketTimeoutException> {
                workaroundHandler.handle("fnr", now(), now()) { request() }
            }
        }
    }

    @Test
    fun `Annen Exception`() {
        val request: () -> Pair<Int, String?> = { throw IllegalArgumentException()}
        val workaroundHandler = WorkaroundHandler()
        (1..3).forEach { _ ->
            assertThrows<IllegalArgumentException> {
                workaroundHandler.handle("fnr", now(), now()) { request() }
            }
        }
    }

    @Test
    fun `GradertAdresseException fra Lovme`() {
        val workaroundHandler = WorkaroundHandler()
        @Language("JSON")
        val response = """
        {
          "url" : "/",
          "message" : "GradertAdresse. Lovme skal ikke  kalles for personer med kode 6/7",
          "cause" : "no.nav.medlemskap.common.exceptions.GradertAdresseException",
          "code" : {
            "value" : 503,
            "description" : "Service Unavailable"
          },
          "callId" : { }
        }
        """
        val request: () -> Pair<Int, String?> = { 503 to response }
        val (responseCode, responseBody) = workaroundHandler.handle("fnr", now(), now()) { request() }
        assertEquals(200, responseCode)
        assertEquals("VetIkke", objectMapper.readTree(responseBody).path("resultat").path("svar").asText())
    }

    @Test
    fun `Annen Exception fra Lovme`() {
        val workaroundHandler = WorkaroundHandler()
        val response = """{ "cause" : "no.nav.medlemskap.common.exceptions.NoeAnnetException" }"""
        val request: () -> Pair<Int, String?> = { 503 to response }
        val (responseCode, responseBody) = workaroundHandler.handle("fnr", now(), now()) { request() }
        assertEquals(503, responseCode)
        assertEquals(response, responseBody)
    }


    @Test
    fun `JA fra Lovme`() {
        val workaroundHandler = WorkaroundHandler()
        @Language("JSON")
        val response = """
        {
          "resultat" : {
            "svar" : "JA"
          }
        }
        """
        val request: () -> Pair<Int, String?> = { 200 to response }
        val (responseCode, responseBody) = workaroundHandler.handle("fnr", now(), now()) { request() }
        assertEquals(200, responseCode)
        assertEquals("JA", objectMapper.readTree(responseBody).path("resultat").path("svar").asText())
    }
    @Test
    fun `VetIkke fra Lovme`() {
        val workaroundHandler = WorkaroundHandler()
        @Language("JSON")
        val response = """
        {
          "resultat" : {
            "svar" : "VetIkke"
          }
        }
        """
        val request: () -> Pair<Int, String?> = { 200 to response }
        val (responseCode, responseBody) = workaroundHandler.handle("fnr", now(), now()) { request() }
        assertEquals(200, responseCode)
        assertEquals("VetIkke", objectMapper.readTree(responseBody).path("resultat").path("svar").asText())
    }

    private companion object {
        private val objectMapper = jacksonObjectMapper()
    }
}