plugins {
  kotlin("jvm") version "2.0.10"
  kotlin("plugin.serialization") version "2.0.10"
  id("maven-publish")
}

group = "dev.surly"
version = "0.1.0"

repositories {
  mavenCentral()
}

dependencies {
  api("com.zaxxer:HikariCP:5.1.0")
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(21)
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])

      groupId = "dev.surly"
      artifactId = "kotlin-minimal-rdms"
      version = "0.1.0"
    }
  }
}
