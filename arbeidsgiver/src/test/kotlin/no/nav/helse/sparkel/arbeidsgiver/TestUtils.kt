package no.nav.helse.sparkel.arbeidsgiver

import com.github.navikt.tbd_libs.test_support.CleanupStrategy
import com.github.navikt.tbd_libs.test_support.DatabaseContainers

val databaseContainer = DatabaseContainers.container("sparkel-arbeidsgiver", CleanupStrategy.tables("inntektsmelding_registrert"))
