plugins {
    id("java-library")
}

repositories.mavenCentral()

tasks.test {
    useJUnitPlatform()
}
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}
