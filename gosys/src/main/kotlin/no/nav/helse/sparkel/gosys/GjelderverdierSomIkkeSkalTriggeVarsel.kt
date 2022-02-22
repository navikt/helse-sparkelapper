package no.nav.helse.sparkel.gosys

enum class GjelderverdierSomIkkeSkalTriggeVarsel(val behandlingstype: String?, val behandlingstema: String?) {
    ANKE("ae0046", null),
    FORKORTET_VENTETID("ae0247", null),
    HJEMSENDT_TIL_NY_BEHANDLING("ae0115", null),
    KLAGE_YRKESSKADE("ae0058","ab0339"),
    LØNNSKOMPENSASJON(null, "ab0438"),
    MANGLENDE_INNBETALING(null, "ab0446"),
    PARTSINNSYN("ae0224", null),
    REFUSJON_RISIKO_SYKEFRAVÆR("ae0121", "ab0200"),
    REFUSJONSKRAV_DAG_4(null, "ab0433"),
    REFUSJONSKRAV_DAG_6(null, "ab0456"),
    RISIKO_SYKEFRAVÆR(null, "ab0200"),
    UNNTAK_FRA_ARBEIDSGIVERPERIODE(null, "ab0338"),
    DIGITAL_SØKNAD_UNNTAK_FRA_ARBEIDSGIVERPERIODE("ae0227", "ab0338"), // Denne har teksten "Fritak fra arbeidsgiverperiode" i Gosys
    KLAGE_UNNTAK_FRA_ARBEIDSGIVERPERIODE("ae0058", "ab0338");

    companion object {
        fun inneholder(behandlingstype: String?, behandlingstema: String?): Boolean {
            values().forEach {
                if (it.behandlingstype == behandlingstype && it.behandlingstema == behandlingstema) return true
            }
            return false
        }
    }
}
