---
layout: userguide
---

# In-App Messaging
In-App Messaging (IAM) module allows app developers to easily configure and display notifications within their app.

![In-App Messaging Sample](images/what-is-inapp.png)

### This page covers:
* [Requirements](#requirements)
* [SDK Integration](#integration)
* [SDK Logic](#logic)
* [Advanced Features](#advanced)
* [Troubleshooting](#troubleshooting)
* [FAQ](#faq)
* [Documentation and Useful Links](#see-also)
* [Change Log](#changelog)

## <a name="requirements"></a> Requirements
### Supported Android Versions
This SDK supports Android API level 23 (Marshmallow) and above.

### In-App Messaging Subscription Key
You must have a subscription key for your application from IAM Dashboard.

## <a name="integration"></a> SDK Integration
### #1 Include Maven Central repo in your project, this should be added in your project root `build.gradle` file.

```groovy
allprojects {
    repositories {
        mavenCentral()
    }
}
```

### #2 Add InAppMessaging SDK in your project dependencies.

Note: InAppMessaging SDK only uses AndroidX libraries. Host apps should migrate to AndroidX to avoid duplicate dependencies.

```groovy
dependencies {
    implementation 'io.github.rakutentech.inappmessaging:inappmessaging:${latest_version}'
}
```
Please refer to [Changelog](#changelog) section for the latest version.

### #3 Target and compile SDK version to 31 or above.
Note: It is required to target and compile to SDK version 31 or above.

```groovy
android {
    compileSdkVersion 31

    defaultConfig {
    // Defines the minimum API level required to run the app.
    minSdkVersion 23
    // Specifies the API level used to test the app.
    targetSdkVersion 31
    }
}
```

### #4 Adding subscription ID and config URL to your app's AndroidManifest.xml file.

```xml
<meta-data
    android:name="com.rakuten.tech.mobile.inappmessaging.subscriptionkey"
    android:value="change-to-your-subsrcription-key"/>

<meta-data
    android:name="com.rakuten.tech.mobile.inappmessaging.configurl"
    android:value="change-to-config-url"/>
```

If you want to enable debug logging in In-App Messaging SDK (tags begins with "IAM_"), add the following meta-data in AndroidManifest.xml file.
```xml
<meta-data
    android:name="com.rakuten.tech.mobile.inappmessaging.debugging"
    android:value="true"/>
```

| Field            | Datatype| Manifest Key                                             | Optional   | Default |
|------------------|---------|----------------------------------------------------------|------------|---------|
| Subscription Key | String  | `com.rakuten.tech.mobile.inappmessaging.subscriptionkey` | ❌         | 🚫      |
| Config URL       | String  | `com.rakuten.tech.mobile.inappmessaging.configurl`       | ❌         | 🚫      |
| Debugging        | boolean | `com.rakuten.tech.mobile.inappmessaging.debugging`       | ✅         | `false` |

#### **Enable and disable the SDK remotely**
We recommend, as good engineering practice, that you integrate with a remote config service so that you can fetch a feature flag, e.g. `Enable_IAM_SDK`, and use its value to dynamically enable/disable the SDK without making an app release. There are many remote config services on the market, both free and paid.

### #5 <a name="info-provider"></a> Creating UserInfoProvider.
Create a new class in your project that implements the following class:
```kotlin
com.rakuten.tech.mobile.inappmessaging.runtime.UserInfoProvider
```

This class serves the purpose of providing basic user information such as user ID, Access Token, and ID Tracking Identifier at runtime. These user information are used by the SDK for campaigns targeting specific users.

```kotlin
class AppUserInfoProvider : UserInfoProvider {

    // This method is optional for Kotlin class
    // If Access Token will not be used, no need to override this method (i.e. default value is "")
    override fun provideAccessToken(): String? {
        return accessToken
    }

    // This method is optional for Kotlin class
    // If User ID will not be used, no need to override this method (i.e. default value is "")
    override fun provideUserId(): String? {
        return userId
    }

    // This method is optional for Kotlin class
    // If ID tracking identifier will not be used, no need to override this method (i.e. default value is "")
    override fun provideIdTrackingIdentifier(): String? {
        return idTrackingIdentifier
    }
}
```

* User ID - The ID when registering a Rakuten account (e.g. email address or username).
* Access Token - This is the token provided by the internal User SDK as the "authentication token" value.
* ID Tracking Identifier - This is the value provided by the internal identity SDK as the "tracking identifier" value.

To help IAM identify users, please keep user information in the preference object up to date.
After logout is complete, please ensure that all `UserInfoProvider` methods in the preference object return `null` or empty string.

**<font color="red">Notes for Rakuten Developers:</font>**
* **Only provide Access token if the user is logged in.**
* **The internal IAM backend only supports production Access token.**

### <a name="configure-sdk"></a> #6 Configuring In-App Messaging SDK.
Host app should configure the SDK, then register the provider containing the user information.
* An optional lambda function callback can be set to receive exceptions that caused failed configuration or non-fatal failures in the SDK.

In your Application class' `onCreate()` method, add:

```kotlin
// Set optional lambda function callback.
// This can be used for analytics and logging of encountered configuration issues.
InAppMessaging.errorCallback = {
    Log.e(TAG, it.localizedMessage, it.cause)
}

// Configure API: configure(context: Context): Boolean
val iamFlag = InAppMessaging.configure(this)

// use flag to enable/disable IAM feature in your app.
if (iamFlag) {
    InAppMessaging.instance().registerPreference(provider)
}
```

The preference object can be set once per app session. IAM SDK will read object's properties on demand.

Preferences are not persisted so this function needs to be called on every launch.

**<font color="red">Notes:</font>**
* Missing Subscription Key or other critical information are some of the possible issues that can be encountered during configuration.
* You can move the `configure()` call inside an `if (<enable IAM-SDK boolean value> == true)` block to control enabling/disabling the SDK.
* If `configure()` is not called, subsequent calls to other public API SDK functions have no effect.

### #7 Registering and unregistering activities.
Only register activities that are allowed to display In-App messages. Your activities will be kept in a `WeakReference` object, so it will not cause any memory leaks. Don't forget to unregister your activities in `onPause()` method.

Please see [Context](#context) feature section to have more control on displaying campaigns.

```kotlin
override fun onResume() {
    super.onResume()
    InAppMessaging.instance().registerMessageDisplayActivity(this)
}

override fun onPause() {
    super.onPause()
    InAppMessaging.instance().unregisterMessageDisplayActivity()
}
```

### #8 Logging events
Host app can log events to InAppMessaging anywhere in your app.

These events will trigger messages with the same event based trigger. Upon receiving logged event, InAppMessaging SDK will start matching it with current campaigns immediately. After a campaign message's trigger events are matched by the logged events, this message will be displayed in the current registered activity. If no activity is registered, it will be displayed in the next registered activity.

### #9 See [advanced features](#advanced) for optional behavior

### Pre-defined event classes:<br/>
### `AppStartEvent`
Host app should log this event on app launch from terminated state. Recommended to log this event in host app's main activity's `Activity#onStart()`.

App Start Event is persistent, meaning, once it's logged it will always satisfy corresponding trigger in a campaign. All subsequent logs of this event are ignored. Campaigns that require only AppStartEvent are shown once per app session.

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onStart() {
        super.onStart()
        InAppMessaging.instance().logEvent(AppStartEvent())
    }
}
```

### `LoginSuccessfulEvent`
Host app should log this every time the user logs in successfully.

```kotlin
InAppMessaging.instance().logEvent(LoginSuccessfulEvent())
```

### `PurchaseSuccessfulEvent`
Host app should log this event after every successful purchase.

```kotlin
InAppMessaging.instance().logEvent(PurchaseSuccessfulEvent())
```

### Custom event class:

### `CustomEvent`
Host app should log this after app-defined states are reached or conditions are met.

Custom events can have attributes with names and values. Attributes can be `integer`, `double`, `String`, `boolean`, or `java.util.Date` type.

* Every custom event requires a name(case insensitive), but doesn't require to add any attributes with the custom event.
* Each custom event attribute also requires a name(case insensitive), and a value.
* Recommended to use English characters only.
* Because the custom event's name will be used when matching campaigns with triggers; therefore, please make sure the actual campaign event's name and attribute's name must match with the logged events to InAppMessaging SDK.

```kotlin
InAppMessaging.instance().logEvent(CustomEvent("search").addAttribute("keyword", "book").addAttribute("number_of_keyword", 1))
```

**<font color="red">Please note:</font> Logging events may trigger InAppMessaging SDK to update current session data if there were changes in the app's user information (see [UserInfoProvider](#info-provider) section for details).**

## <a name="logic"></a> SDK Logic

### Client-side opt-out handling

If user (with valid identifiers in [`UserInfoProvider`](#info-provider) class) opts out from a campaign, that information is saved in user cache locally on the device and the campaign won't be shown again for that user on the same device. The opt-out status is not shared between devices. The same applies for anonymous user.
* Each user has a separate cache container that is persisted in `SharedPreferences`. Each combination of userId and idTrackingIdentifier is treated as a different user including a special - anonymous user - that represents non logged-in user (userId and idTrackingIdentifier are null or empty).

### <a name="max-impression"></a> Client-side max impressions handling

Campaign impressions (displays) are counted locally for each user. Meaning that a campaign with maxImpression value of 3 will be displayed to each user (different identifiers in [`UserInfoProvider`](#info-provider) class) max 3 times. Campaign's max impression number can be modified in the dashboard/backend. Then the SDK, after next ping call, will compare new value with old max impression number and add the difference to the current impression counter. The max impression data is not shared between devices. The same applies for anonymous user.

## <a name="advanced"></a> Advanced Features

### <a name="context"></a> #1 Campaign's context
Contexts are used to add more control on when campaigns are displayed.
A context can be defined as the text inside "[]" within an IAM portal "Campaign Name" e.g. the campaign name is "[ctx1] title" so the context is "ctx1".
Multiple contexts are supported.
In order to handle defined contexts in the code an optional callback is called before a message is displayed:

```kotlin
InAppMessaging.instance().onVerifyContext = { contexts: List<String>, campaignTitle: String -> Boolean
    if /* check your condition e.g. are you on the correct screen to display this message? */ {
        true // campaign message will be displayed
    } else {
        false // campaign message won't be displayed
    }
}
```

### <a name="close-campaign"></a> #2 Close campaign API
There may be cases where apps need to manually close the campaigns without user interaction.

An example is when a different user logs-in and the currently displayed campaign does not target the new user.

It is possible that the new user did not close the campaign (tapping the 'X' button) when logging in. The app can force-close the campaign by calling the `closeMessage()` API.

```kotlin
InAppMessaging.instance().closeMessage()
```

An optional parameter, `clearQueuedCampaigns`, can be set to `true` (`false` by default) which will additionally remove all campaigns that were queued to be displayed.

```kotlin
InAppMessaging.instance().closeMessage(true)
```


**<font color="red">Note:</font> Calling this API will not increment the campaign's impression (i.e not counted as displayed).**

### <a name="custom-font"></a> #3 Custom fonts for campaigns

The SDK can optionally use custom fonts on campaign header and body texts, and button texts. The default Android system font will be used if custom fonts are not added.

To use custom fonts:
1. Add the font files, `ttf` or `otf` format, to the `font` resource folder of your app.
2. To use custom font for the following campaign parts, define a string resource in the app's `res/values/string.xml`:
* for campaign header texts, set font filename to `iam_custom_font_header` resource name
* for campaign body texts, set font filename to `iam_custom_font_body` resource name
* for campaign button texts, set font filename to `iam_custom_font_button` resource name

Note: You can set the same font filename for the different string resources to use the same font.

```bash
...
├── res
     ├── font
          ├── your_app_font.otf // or ttf format
          ├── your_app_other_font.otf // or ttf format
```

in strings.xml:
```xml
    <string name="iam_custom_font_header">your_app_font</string>
    <string name="iam_custom_font_body">your_app_font</string>
    <string name="iam_custom_font_button">your_app_other_font</string>
```

## <a name="troubleshooting"></a> Troubleshooting
### Proguard ParseException
```kotlin
Caused by: java.io.IOException: proguard.ParseException: Unknown option '-if' in line 84 of file
This error will be thrown during compilation if `minifyEnabled = true`, and your Proguard version is below 6.0.
```
<details>
<summary style="cursor: pointer;";>(click to expand)</summary>
Recommendation: Update your project's Android Gradle Plugin to the latest version, it includes the latest version of Proguard.

Less optimal solution: Force Gradle to use the latest version of Proguard(https://sourceforge.net/p/proguard/discussion/182455/thread/89a4d63d/):

```groovy
buildscript {
    ...
    configurations.all {
      resolutionStrategy {
        force 'net.sf.proguard:proguard-gradle:6.0.3'
        force 'net.sf.proguard:proguard-base:6.0.3'
      }
    }
}
```
</details>

### Duplicate class ManifestConfig
Build Error: `java.lang.RuntimeException: Duplicate class com.rakuten.tech.mobile.manifestconfig.annotations.ManifestConfig`
<details>
<summary style="cursor: pointer;";>(click to expand)</summary>

This build error could occur if you are using older versions of other libraries from `com.rakuten.tech.mobile`.
Some of the dependencies in this SDK have changed to a new Group ID of `io.github.rakutentech` (due to the [JCenter shutdown](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/)).
This means that if you have another library in your project which depends on the older dependencies using the Gropu ID `com.rakuten.tech.mobile`, then you will have duplicate classes.

To avoid this, please add the following to your `build.gradle` in order to exclude the old `com.rakuten.tech.mobile` dependencies from your project.

```groovy
configurations.all {
    exclude group: 'com.rakuten.tech.mobile', module: 'manifest-config-processor'
    exclude group: 'com.rakuten.tech.mobile', module: 'manifest-config-annotations'
}
```

</details>

### Other Issues
Rakuten developers experiencing any other problems should refer to the Troubleshooting Guide on the internal developer documentation portal.

## <a name="faq"></a> Frequently Asked Questions
### Q: How do I send message for in staging or testing environment?
When creating campaigns, you can set the versions which the campaign will be applied to your app.
You can set the versions to staging versions (ex. 0.0.1, 1.0.0-staging, 0.x.x, etc.)

### Q: How many times In-App Message should be sent to device? Does it depends on Max Lifetime Impressions?
The max impression is handled by SDK and is bound to user per device.<br/>
1. Scenario- Max impression is set to 2. User does not login with any ID. So It will be shown 2 times.
2. Scenario- Max impression is set to 2. User login with any ID for 2 devices. It will show 2 times for each device.
3. The campaign start time can be shown

Please refer to [max impression handling](#max-impression) for more details.

### Q: Is the campaign displayed if ALL or ANY of triggers are satisfied?
All the events "launch the app event, login event, purchase successful event, custom event" work as AND. It will send to the device only all defined event are triggered.

## <a name="see-also"></a> Documentation and Useful Links
Documents targeting Engineers:

+ [SDK Source Code](https://github.com/rakutentech/android-inappmessaging)

Documents targeting Product Managers:

+ In-App Messaging Dashboard Sign Up(page is coming soon.)

## <a name="changelog"></a> Changelog

### 7.1.0 (2022-06-24)
* SDKCF-5256: Added sending of impression events with campaign details to analytics account.
* SDKCF-4919: Added support for building using Java 11.
* SDKCF-5173: Updated detekt to stricter check.
* SDKCF-5295: Updated SDK Utils version to update default `initOrder` of content provider.

### 7.0.0 (2022-04-25)
* SDKCF-4941: **Breaking Changes:** Updated configuration API to align with iOS.
  - Renamed `init()` API to `configure()`.
  - Removed optional `errorCallback` lambda function parameter in `configure()`, and changed as static variable.
  - Please see [Configuring In-App Messaging SDK section](#configure-sdk) for details and sample code.
* SDKCF-4904: Updated dependencies to fix vulnerability issues.
* SDKCF-5002:  Campaign UX Improvements: Added handling for new flags in campaign payload:
  - Setting to Hide the top right "x" closing button.
  - "End Date" setting in order to have a "Never ends" option.
  - "Max Lifetime impressions" setting in order to have several options (No limits/Only once/Multiple times). By default set it to "Only Once".
* SDKCF-4859: Fixed campaign button boundaries when campaign and button backgrounds have the same color.
* SDKCF-5019: Updated SDK Utils to v1.1.0.
* SDKCF-4860: Added Jetpack Compose sample app.

### 6.1.0 (2022-02-09)
* SDKCF-4470: Updated the layout for close and campaign buttons. Added feature to customize text and button fonts, please see [custom font section](#custom-font) for details.
* SDKCF-4684: Fixed Picasso bitmap retrieval to avoid crash on large images.
* SDKCF-4650: Refactored handling for different responses from endpoint requests for consistency and better logging.
* SDKCF-4690: Changed common code to use SDK Utils: Logger and SharedPreferences
* SDKCF-4799: Fixed reported issues due to version update of SonarQube
* SDKCF-4636: Refactored handling for different responses from endpoint requests for consistency and better logging.
* SDKCF-4729: Added error handling for display permission request.

### 6.0.0 (2021-12-03)
* SDKCF-4151: **Breaking Changes:**
  - Renamed method for providing access token in `UserInfoProvider` interface class from `provideRaeToken` to `provideAccessToken`.
  - Removed `provideRakutenId` method for Rakuten Id in `UserInfoProvider` interface class. Please use `provideUserId` for specific user targeting.
  - All the methods in `UserInfoProvider` class are optional for Kotlin class implementing the interface.
* SDKCF-4468: **Breaking Change:** Removed deprecated updateSession() API.
* SDKCF-4530: Fixed handling for case-sensitivity update for custom event and attribute name.
* SDKCF-4196: Updated dependencies due to JCenter shutdown.
* SDKCF-4190: Updated Kluent dependency version due to deprecated mocking feature.
* SDKCF-4427: Updated compile and target SDK to API 31 (Android 12).
* SDKCF-3978: Updated endpoint response models to fix optional and required parameters.
* SDKCF-4440: Changed image downloader dependency from Fresco to Picasso.
* SDKCF-3979: Added displaying delay between campaign messages.
* SDKCF-3826: Removed deprecated kotlin-android-extensions plugin.
* SDKCF-4117: Added handling for send IAM events to analytics backend if the dependency exists.

### 5.0.0 (2021-09-10)
* SDKCF-4071: **Breaking Change:** Added new method for providing id tracking identifier in `UserInfoProvider` interface class.
  - The new method is optional for Kotlin class implementing the interface.
* SDKCF-4174: Updated support link in documentation.
* SDKCF-4219: Fixed issue regarding incorrect behavior of closeMessage API when queue is not cleared.

### 4.0.0 (2021-08-04)
* SDKCF-3651: Changed Config API call to /GET with query params. This allows the backend to filter requests if required.
* SDKCF-3653: Added handling for Config and Ping API calls for "429 too many requests" response. The SDK will start exponential backoff (plus a random factor) retries to space out the requests to the backend when code 429 is received.
* SDKCF-3655: Handled opt-out and max impression tracking logic solely on SDK. This change reduces the backend's load per request.
* SDKCF-3664: Added support on roll-out percentage for computing if In-App Messaging is enabled. This allows the backend to gradually increase campaign distribution.
* SDKCF-3715: Included subscription key in Config API request header to enable better filtering of requests.
* SDKCF-3742: Fixed opt-out wording in JP and EN for consistency with iOS.
* SDKCF-3820: Added disabling of SDK features when response received from backend is disabled config.
* SDKCF-3908: Changed auto-initialization to explicit init for better control and handling for any initialization issue. Please refer to [SDK Integration](#integration) for details.
* SDKCF-3939: Added recommendation to use a remote feature flag to enable/disable the SDK.
* SDKCF-3916: Added internal handling of OS exception and triggering `errorCallback` if set.
* SDKCF-3957: Increased the "hit area" of the close button.
* SDKCF-3992: Updated minimum SDK to API 23.

### 3.0.0 (2021-03-24)
* SDKCF-3450: Update Fresco dependency to v2.4.0 to fix SoLoader issue.
* SDKCF-3454: Changed Maven Group ID to `io.github.rakutentech.inappmessaging`. You must update your dependency declarations to the following:
  - `io.github.rakutentech.inappmessaging:inappmessaging:3.0.0`
* Migrated publishing to Maven Central due to Bintray/JCenter being [shutdown](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/). You must add `mavenCentral()` to your `repositories`.

### 2.3.0 (2021-02-24)
* SDKCF-3199: Add [`closeMessage` API](#close-campaign) for programmatically closing campaigns without user action.
* SDKCF-3129: Fix close button layout issue in slide-up campaign
* SDKCF-3117: Fix ANDROID_ID crash on AQUOS devices
* SDKCF-3219: Add closing of campaign when activity is unregistered

### 2.2.0 (2020-11-10)
* SDKCF-2870: Allow host app to control if a campaign should be displayed in the current screen (using [contexts](#context))
* SDKCF-2980: Fix Android 11 issue where user are not redirected after tapping a campaign's redirect/deeplink button
* SDKCF-2967: Fix issue for campaigns getting displayed multiple times for campaign triggered by the AppLaunch event
* SDKCF-2872: Fix issue for Slide Up campaign was getting shown again after being closed and when user moved to another tab

### 2.1.0 (2020-09-18)
* SDKCF-2568: Deprecate updateSession() API
  - session update will be done internally when event is triggered and user info was changed
  - will be removed on next major version

### 2.0.0 (2020-06-11)
* SDKCF-2054: Converted In-App Messaging to Kotlin
* SDKCF-1614: Polish the Public API (removed unnecessary public APIs)
* SDKCF-1616: Auto Initialize the SDK
* SDKCF-2342: ID tracking identifier targeting
* SDKCF-2353: Rakuten ID targeting
* SDKCF-2402: Update locale parameter format
* SDKCF-2429: Prevent trigger of Launch App Event multiple times
* SDKCF-1619 SDKCF-1617: Remove unnecessary dependencies (Espresso and Lombok)

### 1.4.0
* Adding feature embedded event in action buttons.
* Fix UI bug on SlideUp messages.
* Fix race condition bug when initializing SDK.

### 1.3.0
* Adding support of Full Screen messages.
* Adding support of Slide Up messages.
* Adding support of message opt-out option for Full Screen and Modal messages.

### 1.2.0
* Adding Gif support.
* Removing RAT Broadcaster dependency, hence completely removing Android Support Libraries from InAppMessaging SDK.
