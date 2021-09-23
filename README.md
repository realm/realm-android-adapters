![Realm](logo.png)

Realm is a mobile database that runs directly inside phones, tablets or wearables.

This repository holds adapters for combining [realm-java](https://github.com/realm/realm-java) (the Realm Android SDK) with Android UI components and framework classes.

Currently supported UI components are:

 * [ListView](https://developer.android.com/reference/android/widget/ListView.html)
 * [RecyclerView](https://developer.android.com/reference/android/support/v7/widget/RecyclerView.html)

## Getting Started

This library only works together with the Realm Android SDK. Please see the
[detailed instructions in our docs](https://docs.mongodb.com/realm/sdk/android/examples/adapters/)
for using this library.

To add the adapters to your project, add the following to you app's dependencies:

```
repositories {
    mavenCentral()
}

dependencies {
    compile 'io.realm:android-adapters:4.1.0'
}
```

This library is only compatible with `realm-java` (the Realm Android SDK) 5.0.0 and above.

## Documentation

Documentation for Realm can be found at [docs.mongodb.com/realm/](https://docs.mongodb.com/realm/).
The Realm SDK documentation can be found at [docs.mongodb.com/realm/sdk/android/](https://docs.mongodb.com/realm/sdk/android/).
The API reference is located at [docs.mongodb.com/realm-sdks/java/latest/](https://docs.mongodb.com/realm-sdks/java/latest/).

## Getting Help

- **Need help with your code?**: Look for previous questions on the [#realm tag](https://stackoverflow.com/questions/tagged/realm?sort=newest) — or [ask a new question](http://stackoverflow.com/questions/ask?tags=realm). We activtely monitor & answer questions on SO!
- **Have a bug to report?** [Open an issue](https://github.com/realm/realm-android-adapters/issues/new). If possible, include the version of Realm, a full log, the Realm file, and a project that shows the issue.
- **Have a feature request?** [Open an issue](https://github.com/realm/realm-android-adapters/issues/new). Tell us what the feature should do, and why you want the feature.
- Sign up for our [**Community Newsletter**](http://eepurl.com/VEKCn) to get regular tips, learn about other use-cases and get alerted of blogposts and tutorials about Realm.

## Using Snapshots

If you want to test recent bugfixes or features that have not been packaged in an official release yet, you can use a **-SNAPSHOT** release of the current development version of Realm via Gradle, available on [OJO](http://oss.jfrog.org/oss-snapshot-local/io/realm/realm-android/)

```gradle
repositories {
    maven {
        url 'http://oss.jfrog.org/artifactory/oss-snapshot-local'
    }
}

dependencies {
    compile 'io.realm:android-adapters:<version>'
}
```

See [version.txt](version.txt) for the latest version number.

## Building Realm Android Adapters

In case you don't want to use the pre-compiled version, you can build the library from source.

Prerequisites:

 * Download [**JDK 7**](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) or [**JDK 8**](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) and install it.
 * Download & install the Android SDK, **Android 7.1 (API 25)** (for example through Android Studio’s **Android SDK Manager**)

Once you have completed all the pre-requisites building the adapter library is done with a single command:

```
./gradlew assemble
```

That command will generate:

 * an `aar` file for the adapter library (`adapters/build/outputs/aar/android-adapters-release.aar`)

### Other Commands

 * `./gradlew monkeyDebug` will run the monkey tests on the example project.
 * `./gradlew javadoc` will create the javadoc for the library.
 * `./gradlew publishToSonatype` will upload artifacts to Maven Central.
 * `./gradlew bintrayUpload` will upload a release to Bintray.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for more details!

This project adheres to the [Contributor Covenant Code of Conduct](https://realm.io/conduct).
By participating, you are expected to uphold this code. Please report
unacceptable behavior to [info@realm.io](mailto:info@realm.io).

## License

Realm Android Adapters is published under the Apache 2.0 license.

## Feedback

**_If you use Realm and are happy with it, all we ask is that you please consider sending out a tweet mentioning [@realm](http://twitter.com/realm), or email [help@realm.io](mailto:help@realm.io) to let us know about it!_**

**_And if you don't like it, please let us know what you would like improved, so we can fix it!_**

![analytics](https://ga-beacon.appspot.com/UA-50247013-2/realm-android-adapters/README?pixel)