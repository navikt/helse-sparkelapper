package no.nav.helse.sparkel.dokumenter

import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.rapids_rivers.RapidApplication

fun main() {
    val env = System.getenv()

    val søknadClient = SøknadClient(
        baseUrl = env.getValue("SOKNAD_API_URL"),
    )

    RapidApplication.create(env).apply {
        val dokumentRiver = DokumentRiver(
            rapidsConnection = this,
            søknadClient = søknadClient,
        )

    }
}

internal interface DokumentClient {
    fun hentDokument(fnr: String, dokumentid: String): JsonNode
}