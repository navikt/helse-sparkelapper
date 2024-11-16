package no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_registrert

import java.util.UUID
import no.nav.helse.sparkel.arbeidsgiver.db.InntektsmeldingRegistrertTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

internal class InntektsmeldingRegistrertRepository(private val db: org.jetbrains.exposed.sql.Database) {

    fun lagre(inntektsmeldingRegistrertDto: InntektsmeldingRegistrertDto): Int =
        transaction(db) {
            InntektsmeldingRegistrertTable.run {
                insert {
                    it[dokumentId] = inntektsmeldingRegistrertDto.dokumentId
                    it[hendelseId] = inntektsmeldingRegistrertDto.hendelseId
                    it[opprettet] = inntektsmeldingRegistrertDto.opprettet
                } get (id)
            }
        }

    fun finnDokumentId(hendelseId: UUID): UUID? = transaction(db) {
        InntektsmeldingRegistrertTable
            .select { InntektsmeldingRegistrertTable.hendelseId eq hendelseId }
            .map { it[InntektsmeldingRegistrertTable.dokumentId] }
            .singleOrNull()
    }
}
