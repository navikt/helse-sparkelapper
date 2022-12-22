rootProject.name = "sparkelapper"
rootDir
    .listFiles()
    ?.filter { it.isDirectory && File(it, "build.gradle.kts").exists() }
    ?.forEach { include(it.name) }

sourceControl {
    gitRepository(java.net.URI("https://github.com/navikt/rapids-and-rivers.git")) {
        producesModule("com.github.navikt:rapids-and-rivers")
    }
}
