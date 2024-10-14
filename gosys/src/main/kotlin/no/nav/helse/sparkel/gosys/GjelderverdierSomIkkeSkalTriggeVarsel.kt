package no.nav.helse.sparkel.gosys

enum class GjelderverdierSomIkkeSkalTriggeVarsel(val behandlingstype: String?, val behandlingstema: String?) {
    ANKE("ae0046", null),
    FORKORTET_VENTETID("ae0247", null),
    HJEMSENDT_TIL_NY_BEHANDLING("ae0115", null),
    LØNNSKOMPENSASJON(null, "ab0438"),
    MANGLENDE_INNBETALING(null, "ab0446"),
    PARTSINNSYN("ae0224", null),
    REFUSJON_RISIKO_SYKEFRAVÆR("ae0121", "ab0200"),
    REFUSJONSKRAV_DAG_4(null, "ab0433"),
    REFUSJONSKRAV_DAG_6(null, "ab0456"),
    RISIKO_SYKEFRAVÆR(null, "ab0200"),
    UNNTAK_FRA_ARBEIDSGIVERPERIODE(null, "ab0338"),
    DIGITAL_SØKNAD_UNNTAK_FRA_ARBEIDSGIVERPERIODE("ae0227", "ab0338"), // Denne har teksten "Fritak fra arbeidsgiverperiode" i Gosys
    KLAGE_UNNTAK_FRA_ARBEIDSGIVERPERIODE("ae0058", "ab0338"),
    TIDLIGERE_HJEMSENDT_SAK("ae0114", null),
    VETERANSAK("ae0117", null),
    MANGLENDE_INNBETALING_PARAGRAF_8_22(null, "ab0446"),
    DOKUMENTINNSYN("ae0042", null),
    ERSTATNINGSKRAV("ae0071", null),
    KLAGE_YRKESSKADE("ae0058","ab0339"),
    YRKESSKADE(null, "ab0339"),
    KLAGE_BESTRIDELSE_AV_SYKMELDING("ae0058", "ab0421"),
    KLAGE_BEHANDLINGSDAGER("ae0058", "ab0471");

    companion object {
        fun inneholder(behandlingstype: String?, behandlingstema: String?): Boolean {
            entries.forEach {
                if (it.behandlingstype == behandlingstype && it.behandlingstema == behandlingstema) return true
            }
            return false
        }
    }
}

enum class OppgavetypeSomIkkeSkalTriggeVarsel(val oppgavetype: String?) {
    NØKKELKONTROLL("NOEK"),
    RETUR("RETUR"),
    KONTROLLERER_UTGÅENDE_SKANNET_DOKUMENT("KON_UTG_SCA_DOK"),
    VURDER_NOTAT("VURD_NOTAT");

    companion object {
        fun inneholder(oppgavetype: String?): Boolean {
            entries.forEach {
                if (it.oppgavetype == oppgavetype) return true
            }
            return false
        }
    }
}

enum class GjelderverdierSomIkkeSkalTriggeVarselHvisOppgavenOverEtÅrGammel(val behandlingstype: String?, val behandlingstema: String?) {
    FEILUTBETALING("ae0161", null),
    FEILUTBETALING_UTLAND("ae0160", null),
    IKKE_OPPRETTET_T_SAK(null, "ab0449");

    companion object {
        fun inneholder(behandlingstype: String?, behandlingstema: String?): Boolean {
            entries.forEach {
                if (it.behandlingstype == behandlingstype && it.behandlingstema == behandlingstema) return true
            }
            return false
        }
    }
}
