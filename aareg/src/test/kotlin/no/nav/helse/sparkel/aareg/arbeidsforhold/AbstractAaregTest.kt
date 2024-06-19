package no.nav.helse.sparkel.aareg.arbeidsforhold

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.http.HttpStatusCode
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.aareg.arbeidsforhold.util.aaregMockClient
import no.nav.helse.sparkel.aareg.arbeidsforhold.util.azureTokenStub
import org.slf4j.LoggerFactory

internal open class AbstractAaregTest {

    protected val rapid = TestRapid()
    protected val sendtMelding get() = rapid.inspektør.message(rapid.inspektør.size - 1)

    protected fun settOppApp(aaregSvar: AaregSvar? = null) {
        val mockAaregClient = AaregClient(
            baseUrl = "http://baseUrl.local",
            scope = "aareg-scope",
            tokenSupplier = azureTokenStub(),
            httpClient = if (aaregSvar != null) aaregMockClient(
                aaregSvar.response, aaregSvar.status
            ) else aaregMockClient()
        )
        ArbeidsforholdLøserV2(rapid, mockAaregClient)
    }

    data class AaregSvar(val response: String, val status: HttpStatusCode)

    protected fun opprettLogglytter() = ListAppender<ILoggingEvent>().apply {
        (LoggerFactory.getLogger(ArbeidsforholdLøserV2::class.java) as Logger).addAppender(this)
        start()
    }
}