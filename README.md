# Nextcloud Single Sign On
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/8aa66fac0af94ef2836d386fad69f199)](https://www.codacy.com/app/Nextcloud/Android-SingleSignOn?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=nextcloud/Android-SingleSignOn&amp;utm_campaign=Badge_Grade)

[![](https://jitpack.io/v/nextcloud/Android-SingleSignOn.svg)](https://jitpack.io/#nextcloud/Android-SingleSignOn)


This library allows you to use accounts as well as the network stack provided by the [nextcloud files app](https://play.google.com/store/apps/details?id=com.nextcloud.client). Therefore you as a developer don't need to worry about asking the user for credentials as well as you don't need to worry about self-signed ssl certificates, two factor authentication, save credential storage etc.

*Please note that the user needs to install the [nextcloud files app](https://play.google.com/store/apps/details?id=com.nextcloud.client) in order to use those features.* While this might seem like a "no-go" for some developers, we still think that using this library is worth consideration as it makes the account handling much faster and safer.

## How to use this library

1) Add this library to your project

```gradle
repositories {
    ...
    maven { url "https://jitpack.io" }

}

dependencies {
	implementation "com.github.nextcloud:Android-SingleSignOn:0.4.1"
}

compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
```

We use some features from Java 8, so your project needs also to be compiled with (at least) this version.

2) To choose an account, include the following code in your login dialog:

From an Activity

```java
private void openAccountChooser() {
        try {
            AccountImporter.pickNewAccount(this);
        } 
        catch (NextcloudFilesAppNotInstalledException e) {
            UiExceptionManager.showDialogForException(this, e);
        } catch (AndroidGetAccountsPermissionNotGranted e) {
            UiExceptionManager.showDialogForException(this, e);
        }
}
```
From a Fragment

```java
private void openAccountChooser() {
        try {
            AccountImporter.pickNewAccount(currentFragment);
        } catch (NextcloudFilesAppNotInstalledException e) {
            UiExceptionManager.showDialogForException(this, e);
        } catch (AndroidGetAccountsPermissionNotGranted e) {
            UiExceptionManager.showDialogForException(this, e);
        }
}
```
3) To handle the result of the Account Chooser, include the following:

From an Activity

```java
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
            AccountImporter.onActivityResult(requestCode, resultCode, data, this, new AccountImporter.IAccountAccessGranted() {

                    NextcloudAPI.ApiConnectedListener callback = new NextcloudAPI.ApiConnectedListener() {
                        @Override
                        public void onConnected() {
                            // ignore this one..
                        }

                        @Override
                        public void onError(Exception ex) {
                            // TODO handle errors
                        }
                    };

                    @Override
                    public void accountAccessGranted(SingleSignOnAccount account) {

                        Context l_context = getApplicationContext();

                        // As this library supports multiple accounts we created some helper methods if you only want to use one.
                        // The following line stores the selected account as the "default" account which can be queried by using
                        // the SingleAccountHelper.getCurrentSingleSignOnAccount(context) method
                        SingleAccountHelper.setCurrentAccount(l_context, account.name);

                        // Get the "default" account
                        SingleSignOnAccount ssoAccount = null;
                        try
                        {
                            ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(l_context);
                        } catch (NextcloudFilesAppAccountNotFoundException e)
                        {
                            UiExceptionManager.showDialogForException(l_context, e);
                        } catch (NoCurrentAccountSelectedException e)
                        {
                            UiExceptionManager.showDialogForException(l_context, e);
                        }
                        
                        NextcloudAPI nextcloudAPI = new NextcloudAPI(l_context, ssoAccount, new GsonBuilder().create(), callback);

                        // TODO ... (see code in section 4 and below)
                    }
                });
}
```

From a Fragment

```java
@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    AccountImporter.onActivityResult(requestCode, resultCode, data, LoginDialogFragment.this, new AccountImporter.IAccountAccessGranted() {
    
        NextcloudAPI.ApiConnectedListener callback = new NextcloudAPI.ApiConnectedListener() {
        @Override
        public void onConnected() { 
            // ignore this one..
        }

        @Override
        public void onError(Exception ex) { 
            // TODO handle errors
        }
    };
    
        @Override
        public void accountAccessGranted(SingleSignOnAccount account) {
            // As this library supports multiple accounts we created some helper methods if you only want to use one.
            // The following line stores the selected account as the "default" account which can be queried by using 
            // the SingleAccountHelper.getCurrentSingleSignOnAccount(context) method
            SingleAccountHelper.setCurrentAccount(getActivity(), account.name);
            
            // Get the "default" account
            SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(context);
            NextcloudAPI nextcloudAPI = new NextcloudAPI(context, ssoAccount, new GsonBuilder().create(), callback);

            // TODO ... (see code in section 4 and below)
        }
    });
}
```
From both an Activity and Fragment

```java
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    AccountImporter.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
}

// Complete example: https://github.com/nextcloud/news-android/blob/master/News-Android-App/src/main/java/de/luhmer/owncloudnewsreader/LoginDialogFragment.java
```

4) How to get account information?

```java
// If you stored the "default" account using setCurrentAccount(...) you can get the account by using the following line:
SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(context);

// Otherwise (for multi-account support): (you'll have to keep track of the account names yourself. Note: this has to be the name of SingleSignOnAccount.name)
AccountImporter.getSingleSignOnAccount(context, accountName);

// ssoAccount.name // Name of the account used in the android account manager
// ssoAccount.username
// ssoAccount.token
// ssoAccount.url
```

5) How to make a network request?

   You'll notice that there is an callback parameter in the constructor of the `NextcloudAPI`.

```java
    public NextcloudAPI(Context context, SingleSignOnAccount account, Gson gson, ApiConnectedListener callback) {
```

    
   You can use this callback to subscribe to errors that might occur during the initialization of the API. You can start making requests to the API as soon as you instantiated the `NextcloudAPI` object. For a minimal example to get started (without retrofit) take a look at section 5.2. The callback method `onConnected` will be called once the connection to the files app is established. You can start making calls to the api before that callback is fired as the library will queue your calls until the connection is established.

   5.1) **Using Retrofit**

   5.1.1) Before using this single sign on library, your interface for your retrofit API might look like this:

```java
    public interface API {

        String mApiEndpoint = "/index.php/apps/news/api/v1-2/";

        @GET("user")
        Observable<UserInfo> user();

        @POST("feeds")
        Call<List<Feed>> createFeed(@Body Map<String, Object> feedMap);

        @DELETE("feeds/{feedId}")
        Completable deleteFeed(@Path("feedId") long feedId);

        …
    }
```

   You might instantiate your retrofit `API` by using something like this: 
   
```java
    public class ApiProvider {

        private API mApi;

        public ApiProvider() {
            mApi = retrofit.create(API.class);
        }
    }
```

   5.1.2) Use of new API using the nextcloud app network stack

```java
    public class ApiProvider {

        private API mApi;

        public ApiProvider(NextcloudAPI.ApiConnectedListener callback) {
           SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(context);
           NextcloudAPI nextcloudAPI = new NextcloudAPI(context, ssoAccount, new GsonBuilder().create(), callback);
           mApi = new NextcloudRetrofitApiBuilder(nextcloudAPI, API.mApiEndpoint).create(API.class);

       }
    }
```
    
   Enjoy! If you're already using retrofit, you don't need to modify your application logic. Just exchange the API and you're good to go!

   Note: If you need a different mapping between your json-structure and your java-structure you might want to create a custom type adapter using `new GsonBuilder().create().registerTypeAdapter(...)`. Take a look at [this](https://github.com/nextcloud/news-android/blob/783836390b4c27aba285bad1441b53154df16685/News-Android-App/src/main/java/de/luhmer/owncloudnewsreader/helper/GsonConfig.java) example for more information.

   5.2) **Without Retrofit**

   `NextcloudAPI` provides a method called `performNetworkRequest(NextcloudRequest request)` that allows you to handle the server response yourself.

```java
    public class MyActivity extends AppCompatActivity {
    
        private NextcloudAPI mNextcloudAPI;

        @Override
        protected void onStart() {
            super.onStart();
            try {
                SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(this);
                mNextcloudAPI = new NextcloudAPI(this, ssoAccount, new GsonBuilder().create(), apiCallback);

                // Start download of file in background thread (otherwise you'll get a NetworkOnMainThreadException)
                new Thread() {
                    @Override
                    public void run() {
                        downloadFile();
                    }
                }.start();
            } catch (NextcloudFilesAppAccountNotFoundException e) {
                // TODO handle errors
            } catch (NoCurrentAccountSelectedException e) {
                // TODO handle errors
            }
        }

        @Override
        protected void onStop() {
            super.onStop();
            // Close Service Connection to Nextcloud Files App and
            // disconnect API from Context (prevent Memory Leak)
            mNextcloudAPI.stop();
        }
        
        private NextcloudAPI.ApiConnectedListener apiCallback = new NextcloudAPI.ApiConnectedListener() {
            @Override
            public void onConnected() {
                // ignore this one..
            }

            @Override
            public void onError(Exception ex) {
                // TODO handle error in your app
            }
        };

        private void downloadFile() {
            NextcloudRequest nextcloudRequest = new NextcloudRequest.Builder()
                    .setMethod("GET")
                    .setUrl(Uri.encode("/remote.php/webdav/sample movie.mp4","/"))
                    .build();

            try {
                InputStream inputStream = mNextcloudAPI.performNetworkRequest(nextcloudRequest);
                while(inputStream.available() > 0) {
                    inputStream.read();
                    // TODO do something useful with the data here..
                    // like writing it to a file..?
                }
                inputStream.close();
            } catch (Exception e) {
                // TODO handle errors
            }
        }
    }
```


6) WebDAV

The following WebDAV Methods are supported: `PROPFIND` / `MKCOL`

The following examples shows how to use the `PROPFIND` method. With a depth of 0.

```java
List<String>depth = new ArrayList<>();
depth.add("0");
header.put("Depth", depth);

NextcloudRequest nextcloudRequest = new NextcloudRequest.Builder()
        .setMethod("PROPFIND")
        .setHeader(header)
        .setUrl(Uri.encode("/remote.php/webdav/"+remotePath,"/"))
        .build();
```

## Additional Info:

In case that you require some sso features that were introduced in a specific nextcloud files app version, you can run a simple version check using the following helper method:

```java
int MIN_NEXTCLOUD_FILES_APP_VERSION_CODE = 30030052;

if (VersionCheckHelper.verifyMinVersion(context, MIN_NEXTCLOUD_FILES_APP_VERSION_CODE)) {
   // Version requirement is satisfied! 
}
``` 

## Nextcloud Conference 2018 Talk (5min)

[![Nextcloud Single Sign On for Android David Luhmer](https://img.youtube.com/vi/gnLOwmrJLUw/0.jpg)](https://www.youtube.com/watch?v=gnLOwmrJLUw)

## Video

![](https://user-images.githubusercontent.com/4489723/41563281-75cbc196-734f-11e8-8b22-7b906363e34a.gif)


## Examples

- [Nextcloud news app](https://github.com/nextcloud/news-android)
    - [API](https://github.com/nextcloud/news-android/blob/master/News-Android-App/src/main/java/de/luhmer/owncloudnewsreader/reader/nextcloud/API_SSO.java)
    - [API-Provider (Dagger)](https://github.com/nextcloud/news-android/blob/master/News-Android-App/src/main/java/de/luhmer/owncloudnewsreader/di/ApiProvider.java#L98)
    - [Login Fragment](https://github.com/nextcloud/news-android/blob/master/News-Android-App/src/main/java/de/luhmer/owncloudnewsreader/LoginDialogFragment.java)




## Flow Diagram

Note that the "Make network request" section in the diagram only shows the workflow if you use the "retrofit" api.

![](doc/NextcloudSingleSignOn.png)

# Translations
We manage translations via [Transifex](https://www.transifex.com/nextcloud/nextcloud/android-singlesignon/). So just request joining the translation team for Android on the site and start translating. All translations will then be automatically pushed to this repository, there is no need for any pull request for translations.
