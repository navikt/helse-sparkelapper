val kotliqueryVersion: String by project
val ojdbcVersion = "21.4.0.0"

dependencies {
    api("com.github.seratch:kotliquery:$kotliqueryVersion")
    api("com.oracle.database.jdbc:ojdbc11:$ojdbcVersion")
}