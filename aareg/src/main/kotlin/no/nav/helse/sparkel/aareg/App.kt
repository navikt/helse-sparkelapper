package no.nav.helse.sparkel.aareg

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.features.json.*
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.aareg.arbeidsforhold.ArbeidsforholdClient
import no.nav.helse.sparkel.aareg.arbeidsforhold.Arbeidsforholdbehovløser
import no.nav.helse.sparkel.aareg.arbeidsforholdV2.AaregClient
import no.nav.helse.sparkel.aareg.arbeidsforholdV2.ArbeidsforholdLøserV2
import no.nav.helse.sparkel.aareg.arbeidsforholdV2.StsRestClient
import no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon.Arbeidsgiverinformasjonsbehovløser
import no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon.OrganisasjonClient
import no.nav.helse.sparkel.aareg.util.CallIdInterceptor
import no.nav.helse.sparkel.aareg.util.KodeverkClient
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.ArbeidsforholdV3
import no.nav.tjeneste.virksomhet.organisasjon.v5.binding.OrganisasjonV5
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.ws.addressing.WSAddressingFeature
import org.apache.cxf.ws.security.trust.STSClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import javax.xml.namespace.QName

val sikkerlogg: Logger = LoggerFactory.getLogger("tjenestekall")

val objectMapper: ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

fun main() {
    val environment = setUpEnvironment()
    val serviceUser = readServiceUserCredentials()
    val app = createApp(environment, serviceUser)
    app.start()
}

internal fun createApp(environment: Environment, serviceUser: ServiceUser): RapidsConnection {
    val stsClientWs = stsClient(environment.stsSoapBaseUrl, serviceUser)

    val organisasjonV5 = setupOrganisasjonV5(environment.organisasjonBaseUrl, stsClientWs)
    val arbeidsforholdV3 = setupArbeidsforholdV3(environment.aaregBaseUrl, stsClientWs)

    val httpClient = HttpClient {
        install(JsonFeature) { serializer = JacksonSerializer() }
    }
    val stsRestClient = StsRestClient(environment.stsBaseUrl, serviceUser, httpClient)

    val kodeverkClient = KodeverkClient(
        httpClient = httpClient,
        kodeverkBaseUrl = environment.kodeverkBaseUrl,
        appName = environment.appName
    )

    val arbeidsforholdClient = ArbeidsforholdClient(arbeidsforholdV3, kodeverkClient)
    val organisasjonClient = OrganisasjonClient(organisasjonV5, kodeverkClient)
    val aaregClient = AaregClient(environment.aaregBaseUrlRest, stsRestClient)

    val rapidsConnection = RapidApplication.create(environment.raw)
    Arbeidsgiverinformasjonsbehovløser(rapidsConnection, organisasjonClient)
    Arbeidsforholdbehovløser(rapidsConnection, arbeidsforholdClient, aaregClient, kodeverkClient)
    ArbeidsforholdLøserV2(rapidsConnection, aaregClient)

    return rapidsConnection
}

private val callIdGenerator: ThreadLocal<String> = ThreadLocal.withInitial {
    UUID.randomUUID().toString()
}

fun setupOrganisasjonV5(organisasjonBaseUrl: String, stsClientWs: STSClient): OrganisasjonV5 =
    JaxWsProxyFactoryBean().apply {
        address = organisasjonBaseUrl
        wsdlURL = "wsdl/no/nav/tjeneste/virksomhet/organisasjon/v5/Binding.wsdl"
        serviceName = QName("http://nav.no/tjeneste/virksomhet/organisasjon/v5/Binding", "Organisasjon_v5")
        endpointName = QName("http://nav.no/tjeneste/virksomhet/organisasjon/v5/Binding", "Organisasjon_v5Port")
        serviceClass = OrganisasjonV5::class.java
        this.features.addAll(listOf(WSAddressingFeature()))
        this.outInterceptors.addAll(listOf(CallIdInterceptor(callIdGenerator::get)))
    }.create(OrganisasjonV5::class.java).apply { stsClientWs.configureFor(this) }

fun setupArbeidsforholdV3(arbeidsforholdBaseUrl: String, stsClientWs: STSClient): ArbeidsforholdV3 =
    JaxWsProxyFactoryBean().apply {
        address = arbeidsforholdBaseUrl
        wsdlURL = "wsdl/no/nav/tjeneste/virksomhet/arbeidsforhold/v3/Binding.wsdl"
        serviceName = QName("http://nav.no/tjeneste/virksomhet/arbeidsforhold/v3/Binding", "Arbeidsforhold_v3")
        endpointName = QName("http://nav.no/tjeneste/virksomhet/arbeidsforhold/v3/Binding", "Arbeidsforhold_v3Port")
        serviceClass = ArbeidsforholdV3::class.java
        this.features.addAll(listOf(WSAddressingFeature()))
        this.outInterceptors.addAll(listOf(CallIdInterceptor(callIdGenerator::get)))
    }.create(ArbeidsforholdV3::class.java).apply { stsClientWs.configureFor(this) }
