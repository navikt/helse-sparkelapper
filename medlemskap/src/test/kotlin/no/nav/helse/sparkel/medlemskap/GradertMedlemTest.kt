package no.nav.helse.sparkel.medlemskap.no.nav.helse.sparkel.medlemskap

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate.now
import no.nav.helse.sparkel.medlemskap.MedlemskapClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GradertMedlemTest {

    @Test
    fun `500 og 3 GradertAdresseException skal tolkes som uavklart medlemsskap`() {
        assertEquals("JA", MedlemskapClient.oversett(superforenkletJaSvar, "", now(), now()).path("resultat").path("svar").asText(), "Oversatte feilaktig en vanlig response")
        assertEquals("VetIkke", MedlemskapClient.oversett(gradertAdressesvar,"", now(), now()).path("resultat").path("svar").asText(), "Oversatte ikke GradertException")
    }

}

private val gradertAdressesvar = jacksonObjectMapper().readTree("""
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
""")

// dette er fryktelig forenklet svar fra medlemskapstjenesten, men
// vi bryr oss bare om svaret
private val superforenkletJaSvar = jacksonObjectMapper().readTree("""
    {
      "resultat" : {
        "svar" : "JA"
      }
    }
""")