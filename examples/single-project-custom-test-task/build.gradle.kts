plugins {
    id("com.code-intelligence.cifuzz")
    id("java-library")
}

repositories.mavenCentral()

val fuzzTest = sourceSets.create("fuzzTest")
val specialTestTask = tasks.register<Test>("specialTestTask") {
    classpath = fuzzTest.runtimeClasspath
    testClassesDirs = fuzzTest.output.classesDirs
}

cifuzz {
    testSourceSet.set(fuzzTest)
    testTask.set(specialTestTask)
}

dependencies {
    "fuzzTestImplementation"(project(path))
}
