plugins {
    kotlin("jvm") version "2.4.21"
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

group = "dev.randomletter"
version = "1.0.0"

val paperVersion = "26.3.build.257-stable"
val kotlinVersion = "2.4.21"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperVersion")

    testImplementation(kotlin("test"))
    testImplementation("io.papermc.paper:paper-api:$paperVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(25)
}

tasks {
    processResources {
        val props = mapOf("version" to project.version, "kotlinVersion" to kotlinVersion)
        inputs.properties(props)
        filesMatching("plugin.yml") { expand(props) }
    }
    test {
        useJUnitPlatform()
    }
    runServer {
        minecraftVersion("26.3")
    }
}
