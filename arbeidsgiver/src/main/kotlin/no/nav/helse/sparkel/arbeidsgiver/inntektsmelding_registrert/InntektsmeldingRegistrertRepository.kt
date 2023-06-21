package no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_registrert

import no.nav.helse.sparkel.arbeidsgiver.db.InntektsmeldingRegistrertTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

internal class InntektsmeldingRegistrertRepository {

    fun lagre(inntektsmeldingRegistrertDto: InntektsmeldingRegistrertDto): Int =
        transaction {
            InntektsmeldingRegistrertTable.run {
                insert {
                    it[dokumentId] = inntektsmeldingRegistrertDto.dokumentId
                    it[hendelseId] = inntektsmeldingRegistrertDto.hendelseId
                    it[opprettet] = inntektsmeldingRegistrertDto.opprettet
                } get (id)
            }
        }
}
