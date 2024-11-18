plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.0.1"
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        val type = providers.gradleProperty("platformType")
        val version = providers.gradleProperty("platformVersion")

        create(type, version)
        bundledPlugin("com.intellij.java")

        instrumentationTools()
        pluginVerifier()
        zipSigner()
    }
}

tasks {
    patchPluginXml {
        untilBuild = "242.*"
    }
}

sourceSets {
    main {
        java {
            srcDirs("src")
        }
        resources {
            srcDirs("resources")
        }
    }
    test {
        java {
            srcDirs("test")
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
