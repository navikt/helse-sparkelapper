package no.nav.helse.sparkel.norg

fun readEnvironment(env: Map<String, String> = System.getenv()) = Environment(
    norg2BaseUrl = env.getValue("NORG2_BASE_URL"),
    norg2Scope = env.getValue("NORG2_SCOPE"),
    pdlUrl = env.getValue("PDL_URL"),
    pdlScope = env.getValue("PDL_SCOPE")
)

data class Environment(
    val norg2BaseUrl: String,
    val norg2Scope: String,
    val pdlUrl: String,
    val pdlScope: String
)
