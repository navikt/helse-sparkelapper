package no.nav.helse.sparkel.arbeidsgiver.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime

object InntektsmeldingRegistrertTable  : Table("inntektsmelding_registrert") {
    val id = integer("id").autoIncrement()
    val dokumentId = uuid("dokument_id")
    val hendelseId = uuid("hendelse_id")
    val opprettet = datetime("opprettet")
}