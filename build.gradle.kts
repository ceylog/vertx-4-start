import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "com.wg.vertx"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenLocal()
  maven ("https://maven.aliyun.com/repository/public/")
  mavenCentral()
}

val vertxVersion = "4.0.0"
val junitJupiterVersion = "5.7.0"

val mainVerticleName = "com.wg.vertx.start.verticle.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClassName = launcherClassName
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-pg-client")
  implementation("io.vertx:vertx-sql-client-templates")
  implementation("org.postgresql:postgresql:42.2.18")
  implementation("io.vertx:vertx-hazelcast")
  implementation("io.vertx:vertx-shell")
  implementation("io.vertx:vertx-config")
  implementation("io.vertx:vertx-tcp-eventbus-bridge")
  implementation("io.vertx:vertx-auth-oauth2")
  implementation("io.vertx:vertx-web-validation")
  implementation("io.vertx:vertx-infinispan")
  implementation("io.vertx:vertx-health-check")
  implementation("io.vertx:vertx-micrometer-metrics")
  implementation("io.vertx:vertx-web-openapi")
  implementation("org.flywaydb:flyway-core:7.5.0")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}
