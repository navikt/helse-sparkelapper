package no.nav.helse.sparkel.vilkarsproving.egenansatt

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate

class NOMTest {

    @ParameterizedTest
    @MethodSource("permutations")
    fun `er ansatt nå`(ansattFom: LocalDate?, ansattTom: LocalDate?, expected: Boolean) {
        assertEquals(expected, erAnsattNå(ansattFom, ansattTom))
    }

    companion object {
        private val nå = LocalDate.now()
        private val iFortiden = nå.minusDays(1)
        private val iFramtiden = nå.plusDays(1)

        @JvmStatic
        fun permutations() = listOf(
            Arguments.of(iFortiden, iFortiden, false),
            Arguments.of(iFortiden, iFramtiden, true),
            Arguments.of(iFramtiden, iFortiden, false),
            Arguments.of(iFramtiden, iFramtiden, false),
            Arguments.of(null, null, false),
            Arguments.of(null, iFortiden, false),
            Arguments.of(null, iFramtiden, false),
            Arguments.of(iFortiden, null, true),
            Arguments.of(iFramtiden, null, false)
        )
    }

}