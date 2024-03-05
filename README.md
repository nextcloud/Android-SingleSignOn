<!--
 ~ SPDX-FileCopyrightText: 2016-2024 Nextcloud GmbH and Nextcloud contributors
 ~ SPDX-License-Identifier: GPL-3.0-or-later
-->
# <img src="https://github.com/nextcloud/Android-SingleSignOn/raw/main/.idea/icon.svg" width="24" height="24" alt="Nextcloud Single Sign On Logo"/> Nextcloud Single Sign On

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/3fe63bb2932243f08dc362fa49c5275b)](https://app.codacy.com/gh/nextcloud/Android-SingleSignOn/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Last release](https://jitpack.io/v/nextcloud/Android-SingleSignOn.svg)](https://jitpack.io/#nextcloud/Android-SingleSignOn)
[![GitHub issues](https://img.shields.io/github/issues/nextcloud/Android-SingleSignOn.svg)](https://github.com/nextcloud/Android-SingleSignOn/issues)
[![GitHub stars](https://img.shields.io/github/stars/nextcloud/Android-SingleSignOn.svg)](https://github.com/nextcloud/Android-SingleSignOn/stargazers)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![REUSE status](https://api.reuse.software/badge/github.com/nextcloud/Android-SingleSignOn)](https://api.reuse.software/info/github.com/nextcloud/Android-SingleSignOn)

This library allows you to use accounts as well as the network stack provided by the [Nextcloud Files app](https://play.google.com/store/apps/details?id=com.nextcloud.client). Therefore you as a developer don't need to worry about asking the user for credentials as well as you don't need to worry about self-signed ssl certificates, two factor authentication, save credential storage etc.

*Please note that the user needs to install the [Nextcloud Files app](https://play.google.com/store/apps/details?id=com.nextcloud.client) in order to use those features.* While this might seem like a "no-go" for some developers, we still think that using this library is worth consideration as it makes the account handling much faster and safer.

- [How to use this library](#how-to-use-this-library)
  - [1) Add this library to your project](#1-add-this-library-to-your-project)
  - [2) To choose an account, include the following code in your login dialog](#2-to-choose-an-account-include-the-following-code-in-your-login-dialog)
  - [3) To handle the result of the Account Chooser, include the following](#3-to-handle-the-result-of-the-account-chooser-include-the-following)
  - [4) How to get account information?](#4-how-to-get-account-information)
  - [5) How to make a network request?](#5-how-to-make-a-network-request)
    - [5.1) Using Retrofit](#51-using-retrofit)
    - [5.2) Without Retrofit](#52-without-retrofit)
    - [5.3) WebDAV](#53-webdav)
- [Additional info](#additional-info)
- [R8/ProGuard](#r8proguard)
- [Security](#security)
- [Media](#media)
  - [Talks at the Nextcloud Conference](#talks-at-the-nextcloud-conference)
  - [Demo video](#demo-video)
- [Known apps](#known-apps)
- [Troubleshooting](#troubleshooting)
- [Flow diagram](#flow-diagram)
- [Translations](#translations)

## How to use this library

You can check out the [sample app](https://github.com/nextcloud/Android-SingleSignOn/tree/master/sample) which uses this library to fetch some information via SSO from a Nextcloud instance.
The sample app uses the [Retrofit approach](#51-using-retrofit). Be aware though, that it is for demonstration purposes only. Exception handling, state management etc. must be implemented depending on your use case.

### 1) Add this library to your project

```gradle
repositories {
    // …
    maven { url "https://jitpack.io" }
}

dependencies {
    // Note: Android Gradle Plugin (AGP) version ≥ 8.2.0 is required.
    implementation "com.github.nextcloud:Android-SingleSignOn:1.0.0"
}
```

### 2) To choose an account, include the following code in your login dialog

```java
private void openAccountChooser() {
    try {
        AccountImporter.pickNewAccount(activityOrFragment);
    } catch (NextcloudFilesAppNotInstalledException | AndroidGetAccountsPermissionNotGranted e) {
        UiExceptionManager.showDialogForException(this, e);
    }
}
```

### 3) To handle the result of the Account Chooser, include the following

```java
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    AccountImporter.onActivityResult(requestCode, resultCode, data, this, new AccountImporter.IAccountAccessGranted() {

        @Override
        public void accountAccessGranted(SingleSignOnAccount account) {
            final var context = getApplicationContext();

            // As this library supports multiple accounts we created some helper methods if you only want to use one.
            // The following line stores the selected account as the "default" account which can be queried by using
            // the SingleAccountHelper.getCurrentSingleSignOnAccount(context) method
            SingleAccountHelper.commitCurrentAccount(context, account.name);

            // Get the "default" account
            SingleSignOnAccount ssoAccount = null;
            try {
                ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(context);
            } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
                UiExceptionManager.showDialogForException(context, e);
            }

            final var nextcloudAPI = new NextcloudAPI(context, ssoAccount, new GsonBuilder().create());

            // TODO … (see code in section 4 and below)
        }
    });
}

@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    AccountImporter.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
}

// Complete example: https://github.com/nextcloud/news-android/blob/890828441ba0c8a9b90afe56f3e08ed63366ece5/News-Android-App/src/main/java/de/luhmer/owncloudnewsreader/LoginDialogActivity.java#L470-L475

```

### 4) How to get account information?

```java
// If you stored the "default" account using setCurrentAccount(…) you can get the account by using the following line:
final var ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(context);

// It is also possible to get the "default" account as a LiveData object:
final var ssoAccount$ = SingleAccountHelper.getCurrentSingleSignOnAccount$(context);

// Otherwise (for multi-account support you'll have to keep track of the account names yourself. Note: this has to be the name of SingleSignOnAccount.name)
AccountImporter.getSingleSignOnAccount(context, accountName);

ssoAccount.name; // Name of the account used in the android account manager
ssoAccount.username;
ssoAccount.token;
ssoAccount.url;
```

### 5) How to make a network request?

```java
public NextcloudAPI(Context context, SingleSignOnAccount account, Gson gson) {
```

You'll notice that there is an optional `ApiConnectedListener` callback parameter in the constructor of the `NextcloudAPI`.
You can use this callback to subscribe to errors that might occur during the initialization of the API.
The callback method `onConnected` will be called once the connection to the files app is established.

ℹ️ You can start making requests to the API before that callback is fired as the library will queue your calls until the connection is established[¹](https://github.com/nextcloud/Android-SingleSignOn/issues/400).

#### 5.1) **Using Retrofit**

##### 5.1.1) Before using this Single Sign On library, your interface for your [Retrofit](https://square.github.io/retrofit/) API might look like this:

```java
public interface API {

    String mApiEndpoint = "/index.php/apps/news/api/v1-2/";

    @GET("user")
    Observable<UserInfo> user();

    // use ParsedResponse, in case you also need the response headers. Works currently only for Observable calls.
    @GET("user")
    Observable<ParsedResponse<UserInfo>> user();

    @POST("feeds")
    Call<List<Feed>> createFeed(@Body Map<String, Object> feedMap);

    @DELETE("feeds/{feedId}")
    Completable deleteFeed(@Path("feedId") long feedId);

    // …
}
```

ℹ️ If your REST endpoint returns an empty body, you need to specify `Observable<EmptyResponse>` / `Call<EmptyResponse>` as return value rather than `Observable<Void>` / `Call<Void>` because ["Nulls are not allowed in \[RxJava\] 2.x."](https://github.com/ReactiveX/RxJava/issues/5775#issuecomment-353544736).

You might instantiate your Retrofit `API` by using something like this:

```java
public class ApiProvider {

    private final API mApi;

    public ApiProvider() {
        mApi = retrofit.create(API.class);
    }
}
```

##### 5.1.2) Use of new API using the nextcloud app network stack

```java
public class ApiProvider {

    private final API mApi;

    public ApiProvider(@NonNull NextcloudAPI.ApiConnectedListener callback) {
       final var ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(context);
       final var nextcloudAPI = new NextcloudAPI(context, ssoAccount, new GsonBuilder().create(), callback);
       mApi = new NextcloudRetrofitApiBuilder(nextcloudAPI, API.mApiEndpoint).create(API.class);
   }
}
```

Enjoy! If you're already using Retrofit, you don't need to modify your application logic. Just exchange the API and you're good to go!

ℹ️ If you need a different mapping between your JSON structure and your Java structure you might want to create a custom type adapter using `new GsonBuilder().create().registerTypeAdapter(…)`. Take a look at [this](https://github.com/nextcloud/news-android/blob/783836390b4c27aba285bad1441b53154df16685/News-Android-App/src/main/java/de/luhmer/owncloudnewsreader/helper/GsonConfig.java) example for more information.

#### 5.2) **Without Retrofit**

`NextcloudAPI` provides a method called `performNetworkRequest(NextcloudRequest request)` that allows you to handle the server response yourself.

```java
public class MyActivity extends AppCompatActivity {

    private NextcloudAPI mNextcloudAPI;

    @Override
    protected void onStart() {
        super.onStart();
        try {
            final var ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(this);
            mNextcloudAPI = new NextcloudAPI(this, ssoAccount, new GsonBuilder().create());

            // Start download of file in background thread (otherwise you'll get a NetworkOnMainThreadException)
            new Thread(this::downloadFile).start();
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            // TODO handle errors
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Close Service Connection to Nextcloud Files App and
        // disconnect API from Context (prevent Memory Leak)
        mNextcloudAPI.close();
    }

    private void downloadFile() {
        final List<Pair<String, String>> parameters = new ArrayList<>();
        parameters.add(new QueryPair("quality", "1024p"));
        parameters.add(new Pair<>("someOtherParameter", "parameterValue"));
        
        final var nextcloudRequest = new NextcloudRequest.Builder()
                .setMethod("GET")
                .setParameter(parameters)
                .setUrl(Uri.encode("/remote.php/webdav/sample movie.mp4","/"))
                .build();

        try (final var inputStream = mNextcloudAPI.performNetworkRequest(nextcloudRequest)) {
            while(inputStream.available() > 0) {
                inputStream.read();
                // TODO do something useful with the data here..
                // like writing it to a file…?
            }
        } catch (Exception e) {
            // TODO handle errors
        }
    }
}
```

#### 5.3) **WebDAV**

Currently the following `WebDAV` Methods are supported: `PROPFIND` / `MKCOL`

The following examples shows how to use the `PROPFIND` method with a depth of 0.

```java
final List<String> depth = new ArrayList<>();
depth.add("0");
header.put("Depth", depth);

final var nextcloudRequest = new NextcloudRequest.Builder()
        .setMethod("PROPFIND")
        .setHeader(header)
        .setUrl(Uri.encode("/remote.php/webdav/" + remotePath, "/"))
        .build();
```

## Additional info

In case that you require some SSO features that were introduced in a specific Nextcloud Files app version, you can run a simple version check using the following helper method:

```java
final int MIN_NEXTCLOUD_FILES_APP_VERSION_CODE = 30030052;

if (VersionCheckHelper.verifyMinVersion(context, MIN_NEXTCLOUD_FILES_APP_VERSION_CODE, FilesAppType.PROD)) {
   // Version requirement is satisfied!
}
```

## R8/ProGuard

R8 and ProGuard rules are bundled into [SSO](lib/consumer-proguard-rules.pro).
The bundled rules do **not** cover enabled obfuscation.
Therefore it is **recommended** to add `-dontobfuscate` to your app-specific proguard rules.

With [R8 full mode](https://r8.googlesource.com/r8/+/refs/heads/master/compatibility-faq.md#r8-full-mode) being enabled by default since [AGP 8.0](https://developer.android.com/build/releases/gradle-plugin#default-changes), you will probably need to handle following app-specific rules yourself (or disable full mode):

### Gson
According to [Gson's sample rules](https://github.com/google/gson/blob/master/examples/android-proguard-example/proguard.cfg#L14), you still need to configure rules for your gson-handled classes.
> ```
> # Application classes that will be serialized/deserialized over Gson
> -keep class com.google.gson.examples.android.model.** { <fields>; }
> ```

### Retrofit
The same applies to classes which you're using in the api from step [5.1.1](#511-before-using-this-single-sign-on-library-your-interface-for-your-retrofit-api-might-look-like-this)
```
# Application classes that will be serialized/deserialized by Retrofit
-keep class com.google.gson.examples.android.model.**
```

If you find working less broad rules, contributions to these rules are welcome!

## Security

Once the user clicks on <kbd>Allow</kbd> in the login dialog, the Nextcloud Files App will generate a token for your app. Only your app is allowed to use that token. Even if another app will get a hold of that token, it won't be able to make any requests to the nextcloud server as the nextcloud files app matches that token against the namespace of your app.

![](doc/NextcloudSSO.png)

![](doc/NextcloudSSOHacker.png)

## Media

### Talks at the Nextcloud Conference

| 2018 (5min) | 2020 (5min) |
| --- | --- |
| [![Nextcloud Single Sign On for Android David Luhmer](https://img.youtube.com/vi/gnLOwmrJLUw/0.jpg)](https://www.youtube.com/watch?v=gnLOwmrJLUw) | [![Nextcloud Single Sign On for Android David Luhmer](https://i.ytimg.com/vi/oQJWAv2wVuc/hqdefault.jpg)](https://www.youtube.com/watch?v=oQJWAv2wVuc) |

### Demo video

![Demo video](https://user-images.githubusercontent.com/4489723/41563281-75cbc196-734f-11e8-8b22-7b906363e34a.gif)

## Known apps

- [Nextcloud News app](https://github.com/nextcloud/news-android)
  - [API](https://github.com/nextcloud/news-android/blob/master/News-Android-App/src/main/java/de/luhmer/owncloudnewsreader/reader/nextcloud/NewsAPI.java)
  - [API-Provider (Dagger)](https://github.com/nextcloud/news-android/blob/master/News-Android-App/src/main/java/de/luhmer/owncloudnewsreader/di/ApiProvider.java#L105-L114)
  - [Login Activity](https://github.com/nextcloud/news-android/blob/master/News-Android-App/src/main/java/de/luhmer/owncloudnewsreader/LoginDialogActivity.java)
- [Nextcloud Notes app](https://github.com/stefan-niedermann/nextcloud-notes)
  - [API](https://github.com/stefan-niedermann/nextcloud-notes/blob/master/app/src/main/java/it/niedermann/owncloud/notes/persistence/ApiProvider.java#L85-L106)
  - [Login](https://github.com/stefan-niedermann/nextcloud-notes/blob/master/app/src/main/java/it/niedermann/owncloud/notes/shared/util/SSOUtil.java#L33)
- [Nextcloud Deck app](https://github.com/stefan-niedermann/nextcloud-deck/)
  - [API](https://github.com/stefan-niedermann/nextcloud-deck/blob/master/app/src/main/java/it/niedermann/nextcloud/deck/api/DeckAPI.java)
  - [Login](https://github.com/stefan-niedermann/nextcloud-deck/blob/master/app/src/main/java/it/niedermann/nextcloud/deck/ui/ImportAccountActivity.java#L77)
- [Nextcloud Bookmarks app](https://gitlab.com/bisada/OCBookmarks)
  - [API](https://gitlab.com/bisada/OCBookmarks/-/blob/master/app/src/main/java/org/schabi/ocbookmarks/REST/OCBookmarksRestConnector.java#L42)
  - [Login](https://gitlab.com/bisada/OCBookmarks/-/blob/master/app/src/main/java/org/schabi/ocbookmarks/MainActivity.java#L261)


## Troubleshooting

If you are experiencing any issues, the following tips might workaround:
- Disable battery optimizations of the Nextcloud Files app, especially [in case of a `NextcloudApiNotRespondingException`](https://github.com/nextcloud/Android-SingleSignOn/issues/162)
- [Permit auto start](https://github.com/stefan-niedermann/nextcloud-deck/issues/660#issuecomment-682002392)
- A quickly appearing and disappearing menu when attempting to select an account is often a hint for an outdated Nextcloud Files app

## Flow Diagram

Note that the "Make network request" section in the diagram only shows the workflow if you use the Retrofit API.

![Flow Diagram](doc/NextcloudSingleSignOn.png)

# Translations

We manage translations via [Transifex](https://app.transifex.com/nextcloud/nextcloud/android-singlesignon/). So just request joining the translation team for Android on the site and start translating. All translations will then be automatically pushed to this repository, there is no need for any pull request for translations.
