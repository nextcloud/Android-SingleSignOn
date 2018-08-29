# Nextcloud Single Sign On

This library allows you to use accounts as well as the network stack provided by the [nextcloud files app](https://play.google.com/store/apps/details?id=com.nextcloud.client). Therefore you as a developer don't need to worry about asking the user for credentials as well as you don't need to worry about self-signed ssl certificates, two factor authentication, save credential storage etc.

*Please note that the user needs to install the [nextcloud files app](https://play.google.com/store/apps/details?id=com.nextcloud.client) in order to use those features.* While this might seem like a "no-go" for some developers, we still think that using this library is worth consideration as it makes the account handling much faster and safer.


>**IMPORTANT NOTE**: As this library is under heavy development right now you'll need to install the nextcloud files app manually on your device. Checkout the `sso` branch of the files app (`git clone -b sso https://github.com/nextcloud/android.git`) and install the app using Android Studio. We would love to get feedback!

## How to use this library

1) Add this library to your project

```gradle
dependencies {
    implementation "com.github.nextcloud:android-SingleSignOn:master-SNAPSHOT"
}
```
2) Add the following permission to your `AndroidManifest.xml`

```xml
<uses-permission android:name="com.nextcloud.android.sso"/>
```

3) To choose an account, include the following code in your login dialog:

```java
private void openAccountChooser() {
    try {
        AccountImporter.PickNewAccount(LoginDialogFragment.this);
    } catch (NextcloudFilesAppNotInstalledException e) {
        UiExceptionManager.ShowDialogForException(getActivity(), e);
    }
}

@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (resultCode == RESULT_OK) {
        if (requestCode == CHOOSE_ACCOUNT_SSO) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            Account account = AccountImporter.GetAccountForName(getActivity(), accountName);
            if(account != null) {
                SingleAccountHelper.SetCurrentAccount(getActivity(), account);
            }
        }
    } else if (resultCode == RESULT_CANCELED) {
        if (requestCode == CHOOSE_ACCOUNT_SSO) {
            // Something went wrong..
        }
    }
}

// Complete example: https://github.com/nextcloud/news-android/blob/master/News-Android-App/src/main/java/de/luhmer/owncloudnewsreader/LoginDialogFragment.java
```

3) How to get account information?

```java
Account account = AccountImporter.GetCurrentAccount(getActivity());
SingleSignOnAccount ssoAccount = AccountImporter.GetAuthTokenInSeparateThread(getActivity(), account);

// ssoAccount.name // Name of the account used in the android account manager
// ssoAccount.username
// ssoAccount.token
// ssoAccount.url
```

4) How to make a network request?

    4.1.1) **Using Retrofit** (see below for instructions without retrofit)

    If you have an interface such as the following:

    ```java
    public interface API {

        @GET("user")
        Observable<UserInfo> user();

        @POST("feeds")
        Call<List<Feed>> createFeed(@Body Map<String, Object> feedMap);

        @DELETE("feeds/{feedId}")
        Completable deleteFeed(@Path("feedId") long feedId);

        …
    }
    ```

    Typical use of API using Retrofit
    ```java
    public class ApiProvider {

        private API mApi;

        public ApiProvider() {
            mApi = retrofit.create(API.class);
        }
    }
    ```

    4.1.2) Use Nextcloud network stack:

    You can implement that interface and use the nextcloud network stack instead of the retrofit one.

    ```java
    public class API_SSO implements API {

        private static final String mApiEndpoint = "/index.php/apps/news/api/v1-2/";
        private NextcloudAPI nextcloudAPI;

        public API_SSO(NextcloudAPI nextcloudAPI) {
            this.nextcloudAPI = nextcloudAPI;
        }

        @Override
        public Observable<UserInfo> user() {
            final Type type = UserInfo.class;
            NextcloudRequest request = new NextcloudRequest.Builder()
                    .setMethod("GET")
                    .setUrl(mApiEndpoint + "user")
                    .build();
            return nextcloudAPI.performRequestObservable(type, request);
        }

        @Override
        public Call<List<Feed>> createFeed(Map<String, Object> feedMap) {
            Type feedListType = new TypeToken<List<Feed>>() {}.getType();
            String body = GsonConfig.GetGson().toJson(feedMap);
            NextcloudRequest request = new NextcloudRequest.Builder()
                    .setMethod("POST")
                    .setUrl(mApiEndpoint + "feeds")
                    .setRequestBody(body)
                    .build();
            return Retrofit2Helper.WrapInCall(nextcloudAPI, request, feedListType);
        }

        @Override
        public Completable deleteFeed(long feedId) {
            final NextcloudRequest request = new NextcloudRequest.Builder()
                    .setMethod("DELETE")
                    .setUrl(mApiEndpoint + "feeds/" + feedId)
                    .build();
            return ReactivexHelper.WrapInCompletable(nextcloudAPI, request);
        }

        …
    }
    ```

    4.1.3) Use of new API using the nextcloud app network stack

    ```java
    public class ApiProvider {

        private API mApi;

        public ApiProvider(NextcloudAPI.ApiConnectedListener callback) {
            Account account = SingleAccountHelper.GetCurrentAccount(context);
            SingleSignOnAccount ssoAccount =
                AccountImporter.GetAuthTokenInSeparateThread(context, account);
            NextcloudAPI nextcloudAPI = new NextcloudAPI(context, ssoAccount, GsonConfig.GetGson(), callback);
            mApi = new API_SSO(nextcloudAPI);
        }
    }
    ```
    Enjoy! If you're already using retrofit, you don't need to modify your application logic. Just exchange the API and you're good to go!

    4.2) **Without Retrofit**

    `NextcloudAPI` provides a method called `performNetworkRequest(NextcloudRequest request)` that allows you to handle the server response yourself.

    ```java
    public class MyActivity extends AppCompatActivity {
    
        private NextcloudAPI mNextcloudAPI;

        @Override
        protected void onStart() {
            Account account = SingleAccountHelper.GetCurrentAccount(context);
            SingleSignOnAccount ssoAccount = AccountImporter.GetAuthTokenInSeparateThread(context, account);
            mNextcloudAPI = new NextcloudAPI(context, ssoAccount, GsonConfig.GetGson(),  new NextcloudAPI.ApiConnectedListener() {
                @Override
                public void onConnected() {
                    downloadFile();
                }

                @Override
                public void onError(Exception ex) {
                    // TODO handle error here..
                }
            });
        }

        @Override
        protected void onStop() {
            // Close Service Connection to Nextcloud Files App and
            // disconnect API from Context (prevent Memory Leak)
            mNextcloudAPI.stop();
        }

        private void downloadFile() {
            NextcloudRequest nextcloudRequest = new NextcloudRequest.Builder()
                    .setMethod("GET")
                    .setUrl("/remote.php/webdav/sample.mp4")
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
                e.printStackTrace();
            }
        }
    }
    ```

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
