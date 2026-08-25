plugins {
    java
}

group = "me.reno"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.5")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}
