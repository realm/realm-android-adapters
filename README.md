![Realm](logo.png)

Realm is a mobile database that runs directly inside phones, tablets or wearables.

This repository holds adapters for combining Realm Java with Android UI components and framework classes.

Currently supported UI components are:

 * [ListView](https://developer.android.com/reference/android/widget/ListView.html)
 * [RecyclerView](https://developer.android.com/reference/android/support/v7/widget/RecyclerView.html)

## Getting Started

This library only works together with Realm Java. Please see the [detailed instructions in our docs](https://docs.mongodb.com/realm/sdk/android/)
to add Realm to your project.

To add the adapters to your project, add the following to you app's dependencies:

```
repositories {
    jcenter()
}

dependencies {
    compile 'io.realm:android-adapters:3.1.0'
}
```

This library is only compatible with Realm Java 3.0.0 and above.

## Documentation

Documentation for Realm can be found [here](https://docs.mongodb.com/realm/sdk/android/).
See also the API reference for [Java](https://docs.mongodb.com/realm-sdks/java/latest/) and [Kotlin extensions](https://docs.mongodb.com/realm-sdks/java/latest/kotlin-extensions/).

## Getting Help

- **Need help with your code?**: Look for previous questions on the [#realm tag](https://stackoverflow.com/questions/tagged/realm?sort=newest) — or [ask a new question](http://stackoverflow.com/questions/ask?tags=realm) or engage in our [Community Forum](https://www.mongodb.com/community/forums/c/realm/realm-sdks/58).
- **Have a bug to report?** [Open an issue](https://github.com/realm/realm-android-adapters/issues/new). If possible, include the version of Realm, a full log, the Realm file, and a project that shows the issue.
- **Have a feature request?** [Open an issue](https://github.com/realm/realm-android-adapters/issues/new). Tell us what the feature should do, and why you want the feature.

## Using Snapshots

If you want to test recent bugfixes or features that have not been packaged in an official release yet, you can use a **-SNAPSHOT** release of the current development version of Realm via Gradle, available on [Sonatype OSS](https://oss.sonatype.org/#nexus-search;quick~realm-android-adapters)


```
buildscript {
    repositories {
        mavenCentral()
        google()
        maven {
            url 'https://oss.sonatype.org/content/repositories/snapshots/'
        }
        jcenter()
    }
    dependencies {
        classpath "io.realm:andriod-adapters:<version>-SNAPSHOT"
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven {
            url 'https://oss.sonatype.org/content/repositories/snapshots/'
        }
        jcenter()
    }
}
```

See [version.txt](version.txt) for the latest version number.

## Building Realm Android Adapters

In case you don't want to use the pre-compiled version, you can build the library from source.

Prerequisites:

 * Download [**JDK 8**](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) from Oracle and install it.
 * Download & install the Android SDK **Build-Tools 29.0.3**, **Android Pie (API 29)** (for example through Android Studio’s **Android SDK Manager**).
 
Once you have completed all the pre-requisites building Realm is done with a simple command

```
./gradlew assemble
```

That command will generate:

 * an aar file for the adapter library in `adapters/build/outputs/aar/android-adapters-release.aar`

### Other Commands

 * `./gradlew monkeyDebug` will run the monkey tests on the example project.
 * `./gradlew javadoc` will create the javadoc for the library.
 * `./gradlew artifactoryPublish` will upload a SNAPSHOT to OJO.
 * `./gradlew bintrayUpload` will upload a release to Bintray.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for more details!

This project adheres to the [MongoDB Code of Conduct](https://www.mongodb.com/community-code-of-conduct).
By participating, you are expected to uphold this code. Please report
unacceptable behavior to [community-conduct@mongodb.com](mailto:community-conduct@mongodb.com).

## License

Realm Android Adapters is published under the Apache 2.0 license.

<img style="width: 0px; height: 0px;" src="https://3eaz4mshcd.execute-api.us-east-1.amazonaws.com/prod?s=https://github.com/realm/realm-android-adapters#README.md">
