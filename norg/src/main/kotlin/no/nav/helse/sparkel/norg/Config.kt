package no.nav.helse.sparkel.norg

fun readEnvironment(env: Map<String, String> = System.getenv()) = Environment(
    norg2BaseUrl = env.getValue("NORG2_BASE_URL"),
    pdlUrl = env.getValue("PDL_URL"),
    pdlScope = env.getValue("PDL_SCOPE")
)

data class Environment(
    val norg2BaseUrl: String,
    val pdlUrl: String,
    val pdlScope: String
)
