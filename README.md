[![Maven Central](https://img.shields.io/maven-central/v/io.github.rakutentech.inappmessaging/inappmessaging)](https://search.maven.org/artifact/io.github.rakutentech.inappmessaging/inappmessaging)
![Build status](https://app.bitrise.io/app/e9cc83da00ffd2b1/status.svg?token=4E3R-Baoxp0iPav7uiw1sA&branch=master)
[![codecov](https://codecov.io/gh/rakutentech/android-inappmessaging/branch/master/graph/badge.svg)](https://codecov.io/gh/rakutentech/android-inappmessaging)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=rakutentech_android-inappmessaging&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=rakutentech_android-inappmessaging)

# In-App Messaging SDK for Android

Provides in-app messaging for Android applications. See the [User Guide](./inappmessaging/USERGUIDE.md) for instructions on implementing in an android application.

## How to build

This repository uses submodules for some configuration, so they must be initialized first.

```bash
$ git submodule init
$ git submodule update
$ ./gradlew assemble
```

## How to test the Sample app

You must first define your API config url and subscription key as either environment variables or as gradle properties (such as in your global `~/.gradle/gradle.properties` file).

```
IAM_SUBSCRIPTION_KEY=your_subscription_key
CONFIG_URL=https://www.example.com/
```

## How to use it

Currently we do not host any public APIs but you can create your own APIs and configure the SDK to use those.

## Contributing

See [Contribution guidelines](./CONTRIBUTING.md)
