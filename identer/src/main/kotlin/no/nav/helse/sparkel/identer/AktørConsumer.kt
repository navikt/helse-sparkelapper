package no.nav.helse.sparkel.identer

import no.nav.helse.rapids_rivers.RapidsConnection
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.time.Duration
import no.nav.helse.sparkel.identer.db.IdentifikatorDao

internal class AktørConsumer(
    private val rapidConnection: RapidsConnection,
    private val kafkaConsumer: KafkaConsumer<ByteArray, GenericRecord>,
    private val identifikatorDao: IdentifikatorDao,
) : AutoCloseable, Runnable {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private var konsumerer = true

    override fun run() {
        log.info("AktørConsumer starter opp")
        try {
            while (konsumerer) {
                val records = kafkaConsumer.poll(Duration.ofSeconds(10))
                log.info("Pollet og mottok ${records.count()} meldinger.")
                records.forEach {
                    val key = String(it.key())
                    it.value()?.also { genericRecord ->
                        val aktørV2 = parseAktørMessage(genericRecord, key)
                        aktørV2.gjeldendeFolkeregisterident()?.also {
                            try {
                                identifikatorDao.lagreAktør(aktørV2)
                            } catch (exception: Exception) {
                                sikkerlogg.error("Feilet ved forsøk på å lagre aktør: $aktørV2", exception)
                                throw exception
                            }
                        } ?: sikkerlogg.info("Fant ikke FNR/DNR på melding med key=$key, ignorerer melding.")
                    } ?: sikkerlogg.info("Value var null på melding med key=$key, ignorerer melding.")
                }
            }
        } catch (exception: Exception) {
            log.error("Feilet under konsumering av aktørhendelse", exception)
            throw exception
        } finally {
            close()
            rapidConnection.stop()
        }
    }

    override fun close() {
        konsumerer = false
    }
}

fun parseAktørMessage(record: GenericRecord, key: String): AktørV2 {
    @Suppress("UNCHECKED_CAST")
    val identifikatorRecords = record.get("identifikatorer") as List<GenericRecord>
    return AktørV2(
        identifikatorer = identifikatorRecords
            .filter { Type.valueOf(it.get("type").toString()) != Type.NPID }
            .map {
                Identifikator(
                    idnummer = it.get("idnummer").toString(),
                    type = Type.valueOf(it.get("type").toString()),
                    gjeldende = it.get("gjeldende").toString().toBoolean()
                )
            }.toList(),
        key = key
            .replace("\u0000", "")
            .replace("\u003e", "")
            .replace("\u001a", "")
            .replace("\u0016", "")
    )
}
