package no.nav.helse.sparkel.sykepengeperioderapi

import io.ktor.events.Events
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import no.nav.helse.sparkel.sykepengeperioderapi.ConfiguredCIO.customParallelism

internal object ConfiguredCIO: ApplicationEngineFactory<CIOApplicationEngine, CIOApplicationEngine.Configuration> {
    // Denne er default `Runtime.getRuntime().availableProcessors()` som gir 3 i prod.
    // -> Gjeldende config er da connectionGroupSize=2, workerGroupSize=2, callGroupSize=3
    // -> Nå connectionGroupSize=9, workerGroupSize=9, callGroupSize=16
    private const val customParallelism = 16

    override fun configuration(configure: CIOApplicationEngine.Configuration.() -> Unit): CIOApplicationEngine.Configuration {
        return CIOApplicationEngine.Configuration()
            .apply(configure)
            .apply {
                connectionGroupSize = customParallelism / 2 + 1
                workerGroupSize  = customParallelism / 2 + 1
                callGroupSize = customParallelism
            }
    }

    override fun create(
        environment: ApplicationEnvironment,
        monitor: Events,
        developmentMode: Boolean,
        configuration: CIOApplicationEngine.Configuration,
        applicationProvider: () -> Application
    ): CIOApplicationEngine {
        return CIOApplicationEngine(environment, monitor, developmentMode, configuration, applicationProvider)
    }
}
