plugins {
    alias(libs.plugins.jetbrainsKotlinJvm)
    id("java-library")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    testImplementation(libs.kotestRunner)
    testImplementation(libs.kotestAssertions)
    testImplementation(libs.kotestProperty)
}
