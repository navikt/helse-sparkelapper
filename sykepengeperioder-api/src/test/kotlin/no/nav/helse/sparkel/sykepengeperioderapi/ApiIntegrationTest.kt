package no.nav.helse.sparkel.sykepengeperioderapi

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.sql.DataSource
import kotlin.test.assertEquals
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.sparkel.infotrygd.Fnr
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.testcontainers.oracle.OracleContainer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiIntegrationTest {

    private val oracleContainer = OracleContainer("gvenzl/oracle-free:23-slim-faststart").also { it.start() }
    private val oauthServer = MockOAuth2Server().also { it.start() }
    private val dataSource: HikariDataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = oracleContainer.jdbcUrl
        username = oracleContainer.username
        password = oracleContainer.password
        maximumPoolSize = 5
    })

    init {
        opprettTabeller(dataSource)
    }

    @AfterAll
    fun tearDown() {
        dataSource.close()
        oauthServer.shutdown()
        oracleContainer.stop()
    }

    // -------------------------------------------------------------------------
    // Scenarios
    // -------------------------------------------------------------------------

    @Test
    fun `tomt svar når ingen utbetalinger finnes for perioden`() = withServer {
        val fnr = randomFnr()

        val response = post("/") {
            header(HttpHeaders.Authorization, "Bearer ${token()}")
            contentType(ContentType.Application.Json)
            setBody("""{"personidentifikatorer":["$fnr"],"fom":"2018-01-01","tom":"2018-12-31"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        JSONAssert.assertEquals(
            """{"utbetaltePerioder":[]}""",
            response.bodyAsText(),
            JSONCompareMode.NON_EXTENSIBLE
        )
    }

    @Test
    fun `én utbetaling returneres med korrekte felter`() = withServer {
        val fnr = randomFnr()
        insertPeriode(fnr, seq = 1, fom = LocalDate.of(2018, 1, 1), tom = LocalDate.of(2018, 1, 31))
        insertUtbetaling(fnr, seq = 1, fom = LocalDate.of(2018, 1, 1), tom = LocalDate.of(2018, 1, 31), grad = 80, orgnr = "123456789")

        val response = post("/") {
            header(HttpHeaders.Authorization, "Bearer ${token()}")
            contentType(ContentType.Application.Json)
            setBody("""{"personidentifikatorer":["$fnr"],"fom":"2018-01-01","tom":"2018-12-31"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        JSONAssert.assertEquals(
            """
            {
                "utbetaltePerioder": [{
                    "personidentifikator": "$fnr",
                    "organisasjonsnummer": "123456789",
                    "fom": "2018-01-01",
                    "tom": "2018-01-31",
                    "grad": 80,
                    "tags": []
                }]
            }
            """,
            response.bodyAsText(),
            JSONCompareMode.NON_EXTENSIBLE
        )
    }

    @Test
    fun `to overlappende utbetalinger med grad 100 på ulike arbeidsgivere gir UsikkerGrad-tag på begge`() = withServer {
        val fnr = randomFnr()
        insertPeriode(fnr, seq = 1, fom = LocalDate.of(2018, 1, 1), tom = LocalDate.of(2018, 1, 31))
        insertPeriode(fnr, seq = 2, fom = LocalDate.of(2018, 1, 10), tom = LocalDate.of(2018, 2, 10))
        insertUtbetaling(fnr, seq = 1, fom = LocalDate.of(2018, 1, 1), tom = LocalDate.of(2018, 1, 31), grad = 100, orgnr = "111111111")
        insertUtbetaling(fnr, seq = 2, fom = LocalDate.of(2018, 1, 10), tom = LocalDate.of(2018, 2, 10), grad = 100, orgnr = "222222222")

        val response = post("/") {
            header(HttpHeaders.Authorization, "Bearer ${token()}")
            contentType(ContentType.Application.Json)
            setBody("""{"personidentifikatorer":["$fnr"],"fom":"2018-01-01","tom":"2018-12-31"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        JSONAssert.assertEquals(
            """
            {
                "utbetaltePerioder": [
                    {
                        "personidentifikator": "$fnr",
                        "organisasjonsnummer": "111111111",
                        "fom": "2018-01-01",
                        "tom": "2018-01-31",
                        "grad": 100,
                        "tags": ["UsikkerGrad"]
                    },
                    {
                        "personidentifikator": "$fnr",
                        "organisasjonsnummer": "222222222",
                        "fom": "2018-01-10",
                        "tom": "2018-02-10",
                        "grad": 100,
                        "tags": ["UsikkerGrad"]
                    }
                ]
            }
            """,
            response.bodyAsText(),
            JSONCompareMode.NON_EXTENSIBLE
        )
    }

    @Test
    fun `utbetalinger for to fnr i én forespørsel returnerer begges perioder`() = withServer {
        val fnr1 = randomFnr()
        val fnr2 = randomFnr()
        insertPeriode(fnr1, seq = 1, fom = LocalDate.of(2018, 1, 1), tom = LocalDate.of(2018, 1, 31))
        insertUtbetaling(fnr1, seq = 1, fom = LocalDate.of(2018, 1, 1), tom = LocalDate.of(2018, 1, 31), grad = 100, orgnr = "111111111")
        insertPeriode(fnr2, seq = 1, fom = LocalDate.of(2018, 6, 1), tom = LocalDate.of(2018, 6, 30))
        insertUtbetaling(fnr2, seq = 1, fom = LocalDate.of(2018, 6, 1), tom = LocalDate.of(2018, 6, 30), grad = 50, orgnr = "222222222")

        val response = post("/") {
            header(HttpHeaders.Authorization, "Bearer ${token()}")
            contentType(ContentType.Application.Json)
            setBody("""{"personidentifikatorer":["$fnr1","$fnr2"],"fom":"2018-01-01","tom":"2018-12-31"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        JSONAssert.assertEquals(
            """
            {
                "utbetaltePerioder": [
                    {
                        "personidentifikator": "$fnr1",
                        "organisasjonsnummer": "111111111",
                        "fom": "2018-01-01",
                        "tom": "2018-01-31",
                        "grad": 100,
                        "tags": []
                    },
                    {
                        "personidentifikator": "$fnr2",
                        "organisasjonsnummer": "222222222",
                        "fom": "2018-06-01",
                        "tom": "2018-06-30",
                        "grad": 50,
                        "tags": []
                    }
                ]
            }
            """,
            response.bodyAsText(),
            JSONCompareMode.NON_EXTENSIBLE
        )
    }

    @Test
    fun `forespørsel uten token gir 401`() = withServer {
        val response = post("/") {
            contentType(ContentType.Application.Json)
            setBody("""{"personidentifikatorer":["12345678901"],"fom":"2018-01-01","tom":"2018-12-31"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `utbetalinger med periodeType som ikke er en utbetalingstype filtreres bort som standard`() = withServer {
        val fnr = randomFnr()
        insertPeriode(fnr, seq = 1, fom = LocalDate.of(2018, 1, 1), tom = LocalDate.of(2018, 1, 31))
        insertUtbetaling(fnr, seq = 1, fom = LocalDate.of(2018, 1, 1), tom = LocalDate.of(2018, 1, 31), grad = 80, orgnr = "123456789", periodeType = "2")

        val response = post("/") {
            header(HttpHeaders.Authorization, "Bearer ${token()}")
            contentType(ContentType.Application.Json)
            setBody("""{"personidentifikatorer":["$fnr"],"fom":"2018-01-01","tom":"2018-12-31"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        JSONAssert.assertEquals(
            """{"utbetaltePerioder":[]}""",
            response.bodyAsText(),
            JSONCompareMode.NON_EXTENSIBLE
        )
    }

    @Test
    fun `utbetalinger med periodeType som ikke er en utbetalingstype inkluderes når inkluderAllePeriodetyper er true`() = withServer {
        val fnr = randomFnr()
        insertPeriode(fnr, seq = 1, fom = LocalDate.of(2018, 1, 1), tom = LocalDate.of(2018, 1, 31))
        insertUtbetaling(fnr, seq = 1, fom = LocalDate.of(2018, 1, 1), tom = LocalDate.of(2018, 1, 31), grad = 80, orgnr = "123456789", periodeType = "2")

        val response = post("/") {
            header(HttpHeaders.Authorization, "Bearer ${token()}")
            contentType(ContentType.Application.Json)
            setBody("""{"personidentifikatorer":["$fnr"],"fom":"2018-01-01","tom":"2018-12-31","inkluderAllePeriodetyper":true}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        JSONAssert.assertEquals(
            """
            {
                "utbetaltePerioder": [{
                    "personidentifikator": "$fnr",
                    "organisasjonsnummer": "123456789",
                    "fom": "2018-01-01",
                    "tom": "2018-01-31",
                    "grad": 80,
                    "tags": []
                }]
            }
            """,
            response.bodyAsText(),
            JSONCompareMode.NON_EXTENSIBLE
        )
    }

    // -------------------------------------------------------------------------
    // Infrastructure helpers
    // -------------------------------------------------------------------------

    private fun withServer(block: suspend HttpClient.() -> Unit) {
        testApplication {
            application {
                sykepengeperioderApi(
                    dataSource = dataSource,
                    jwksUri = oauthServer.jwksUrl(ISSUER_ID).toString(),
                    issuer = oauthServer.issuerUrl(ISSUER_ID).toString(),
                    audience = AUDIENCE
                )
            }
            client.block()
        }
    }

    private fun token() = oauthServer.issueToken(
        issuerId = ISSUER_ID,
        audience = AUDIENCE
    ).serialize()

    // -------------------------------------------------------------------------
    // Test data helpers
    // -------------------------------------------------------------------------

    private fun insertPeriode(fnr: String, seq: Int, fom: LocalDate, tom: LocalDate) {
        val itFnr = Fnr(fnr).formatAsITFnr()
        sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    """
                    INSERT INTO is_periode_10 (
                        is01_personkey, f_nr, tk_nr, is10_arbufoer_seq,
                        is10_arbufoer, is10_arbufoer_tom,
                        is10_arbkat, is10_sanksjonsdager, is10_stoenads_type
                    ) VALUES (:seq, :fnr, '0300', :seq, :fom, :tom, '01', 0, '  ')
                    """,
                    mapOf(
                        "seq" to seq,
                        "fnr" to itFnr,
                        "fom" to fom.toInfotrygdInt(),
                        "tom" to tom.toInfotrygdInt()
                    )
                ).asUpdate
            )
        }
    }

    private fun insertUtbetaling(fnr: String, seq: Int, fom: LocalDate, tom: LocalDate, grad: Int, orgnr: String, periodeType: String = "0") {
        val itFnr = Fnr(fnr).formatAsITFnr()
        sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    """
                    INSERT INTO is_utbetaling_15 (
                        f_nr, is10_arbufoer_seq, is15_korr,
                        is15_utbetfom, is15_utbettom,
                        is15_grad, is15_op, is15_dsats, is15_type, is15_arbgivnr
                    ) VALUES (:fnr, :seq, 'N', :fom, :tom, :grad, 'AL', 1000, :periodeType, :orgnr)
                    """,
                    mapOf(
                        "fnr" to itFnr,
                        "seq" to seq,
                        "fom" to fom.toInfotrygdInt(),
                        "tom" to tom.toInfotrygdInt(),
                        "grad" to grad.toString(),
                        "orgnr" to orgnr,
                        "periodeType" to periodeType
                    )
                ).asUpdate
            )
        }
    }

    companion object {
        private const val ISSUER_ID = "test-issuer"
        private const val AUDIENCE = "test-audience"

        private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd")

        fun randomFnr(): String = (10000000000L..99999999999L).random().toString()

        private fun LocalDate.toInfotrygdInt(): Int = format(DATE_FORMAT).toInt()

        fun opprettTabeller(dataSource: DataSource) {
            sessionOf(dataSource).use { session ->
                session.execute(
                    queryOf(
                        """
                        CREATE TABLE is_periode_10 (
                            is01_personkey        NUMBER,
                            f_nr                  VARCHAR2(15)  NOT NULL,
                            tk_nr                 VARCHAR2(4),
                            is10_arbufoer_seq     NUMBER        NOT NULL,
                            is10_arbufoer         NUMBER,
                            is10_arbufoer_tom     NUMBER,
                            is10_ufoeregrad       VARCHAR2(6),
                            is10_max              NUMBER,
                            is10_arbper           VARCHAR2(2),
                            is10_ferie_fom        NUMBER,
                            is10_ferie_tom        NUMBER,
                            is10_ferie_fom2       NUMBER,
                            is10_ferie_tom2       NUMBER,
                            is10_stans            VARCHAR2(4),
                            is10_unntak_aktivitet VARCHAR2(4),
                            is10_arbkat           VARCHAR2(2)   NOT NULL,
                            is10_arbkat_99        VARCHAR2(2),
                            is10_sanksjon_fom     NUMBER,
                            is10_sanksjon_tom     NUMBER,
                            is10_sanksjon_bekreftet VARCHAR2(2),
                            is10_sanksjonsdager   NUMBER        DEFAULT 0 NOT NULL,
                            is10_stoppdato        NUMBER,
                            is10_legenavn         VARCHAR2(25),
                            is10_behdato          NUMBER,
                            is10_skadeart         VARCHAR2(2),
                            is10_skdato           NUMBER,
                            is10_skm_mott         NUMBER,
                            is10_stoenads_type    VARCHAR2(2)   DEFAULT '  ',
                            is10_frisk            VARCHAR2(2)
                        )
                        """
                    )
                )
                session.execute(
                    queryOf(
                        """
                        CREATE TABLE is_utbetaling_15 (
                            f_nr              VARCHAR2(15)  NOT NULL,
                            is10_arbufoer_seq NUMBER        NOT NULL,
                            is15_korr         VARCHAR2(4),
                            is15_utbetfom     NUMBER,
                            is15_utbettom     NUMBER,
                            is15_grad         VARCHAR2(6)   NOT NULL,
                            is15_op           VARCHAR2(4)   NOT NULL,
                            is15_utbetdato    NUMBER,
                            is15_dsats        NUMBER,
                            is15_type         VARCHAR2(2)   NOT NULL,
                            is15_arbgivnr     VARCHAR2(15)  NOT NULL
                        )
                        """
                    )
                )
            }
        }
    }
}
