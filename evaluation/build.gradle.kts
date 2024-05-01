plugins {
    alias(libs.plugins.jetbrainsKotlinJvm)
    id("java-library")
    alias(libs.plugins.kotlinSerialization)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    implementation(libs.kotlinSerialization)
    testImplementation(libs.kotestRunner)
    testImplementation(libs.kotestAssertions)
    testImplementation(libs.kotestProperty)
}
