plugins {
	java
	id("org.springframework.boot") version "3.5.14"
	id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "8.6"
}

group = "dev.shvetsov.temporal"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

val temporalSdkVersion = "1.33.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.temporal:temporal-spring-boot-starter:$temporalSdkVersion")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.temporal:temporal-testing:$temporalSdkVersion")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaExec> {
    jvmArgs = listOf(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.time=ALL-UNNAMED"
    )
}

tasks.withType<Test> {
	useJUnitPlatform()
    jvmArgs = listOf(
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.time=ALL-UNNAMED"
    )
}

tasks.register<JavaExec>("runWorker") {
    group = "application"
    description = "Runs Temporal Worker"
    mainClass = "dev.shvetsov.temporal.TemporalSpringBoot4DemoApplication"
    classpath = sourceSets.main.get().runtimeClasspath
}
