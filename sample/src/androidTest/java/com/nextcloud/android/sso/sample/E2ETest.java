package com.nextcloud.android.sso.sample;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.uiautomator.Until.hasObject;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * <h1>Setup</h1>
 * <h2>CI / CD</h2>
 * <p>No manual configuration needs to be done because the setup already happens in the <code>e2e.yml</code> file.</p>
 * <h2>Local</h2>
 * <ol>
 *     <li>Set {@link #CONFIG_SERVER_URL}, {@link #CONFIG_USERNAME}, {@link #CONFIG_PASSWORD} and {@link #CONFIG_DISPLAY_NAME}. The Nextcloud instance must exist and be reachable.</li>
 *     <li>Remove any existing installation of the Nextcloud files app</li>
 *     <li>Install the <a href="https://download.nextcloud.com/android/dev/latest.apk">Dev-Version of the Nextcloud files app</a></li>
 *     <li>Grant the <code>android.permission.READ_EXTERNAL_STORAGE</code> permission to the Nextcloud files app</li>
 * </ol>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class E2ETest {
    private static final String CONFIG_SERVER_URL = "http://172.17.0.1:8080";
    private static final String CONFIG_USERNAME = "Test";
    private static final String CONFIG_DISPLAY_NAME = "Test";
    private static final String CONFIG_PASSWORD = "Test";

    private static final String TAG = "E2E";
    private static final int TIMEOUT = 60_000;

    private UiDevice mDevice;

    private static final String APP_SAMPLE = BuildConfig.APPLICATION_ID;
    // TODO This should be passed as argument
    private static final String APP_NEXTCLOUD = "com.nextcloud.android.beta";

    @Before
    public void before() {
        mDevice = UiDevice.getInstance(getInstrumentation());
    }

    @Test
    public void test_00_configureNextcloudAccount() throws UiObjectNotFoundException, InterruptedException {
        Log.i(TAG, "Configure Nextcloud account");

        final var context = getInstrumentation().getContext();
        final var packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(APP_NEXTCLOUD, 0);
            Log.i(TAG, "Nextcloud APK is installed (checking on runtime)");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Nextcloud APK is NOT installed (checking on runtime");
        }

        launch(APP_NEXTCLOUD);

        final var loginButton = mDevice.findObject(new UiSelector().textContains("Log in"));
        loginButton.waitForExists(TIMEOUT);
        Log.d(TAG, "Login Button exists. Clicking on it…");
        loginButton.click();
        Log.d(TAG, "Login Button clicked.");

        final var urlInput = mDevice.findObject(new UiSelector().focused(true));
        urlInput.waitForExists(TIMEOUT);
        Log.d(TAG, "URL input exists.");
        Log.d(TAG, "Entering URL…");
        urlInput.setText(CONFIG_SERVER_URL);
        Log.d(TAG, "URL entered.");

        Log.d(TAG, "Pressing enter…");
        mDevice.pressEnter();
        Log.d(TAG, "Enter pressed.");

        final var webView = mDevice.findObject(new UiSelector().instance(0).className(WebView.class));
        Log.d(TAG, "Waiting for WebView…");
//        mDevice.wait(findObject(By.clazz(WebView.class)), TIMEOUT);
        webView.waitForExists(TIMEOUT);
        Log.d(TAG, "WebView exists.");

        final var webViewLoginButton = mDevice.findObject(new UiSelector()
                .instance(0)
                .className(Button.class));
        Log.d(TAG, "Waiting for WebView Login Button…");
        webViewLoginButton.waitForExists(TIMEOUT);
        Log.d(TAG, "WebView Login Button exists. Clicking on it…");

        // TODO Find better way to scroll the Login button to the visible area
        // Log.d(TAG, "Scroll to bottom of WebView…");
        // mDevice.findObject(By.clazz(WebView.class)).swipe(Direction.UP, 1f);
        // Log.d(TAG, "Finished scrolling");
        webViewLoginButton.dragTo(0, 0, 40);

        webViewLoginButton.click();

        final var usernameInput = mDevice.findObject(new UiSelector()
                .instance(0)
                .className(EditText.class));
        Log.d(TAG, "Waiting for Username Input…");
        usernameInput.waitForExists(TIMEOUT);
        Log.d(TAG, "Username Input exists. Setting text…");
        usernameInput.setText(CONFIG_USERNAME);
        Log.d(TAG, "Username has been set.");

        final var passwordInput = mDevice.findObject(new UiSelector()
                .instance(1)
                .className(EditText.class));
        Log.d(TAG, "Waiting for Password Input…");
        passwordInput.waitForExists(TIMEOUT);
        Log.d(TAG, "Password Input exists. Setting text…");
        passwordInput.setText(CONFIG_PASSWORD);

        // mDevice.pressEnter();
        final var webViewSubmitButton = mDevice.findObject(new UiSelector()
                .instance(1) // First button is password visibility toggle
                .className(Button.class));
        Log.d(TAG, "Waiting for WebView Submit Button…");
        webViewSubmitButton.waitForExists(TIMEOUT);
        Log.d(TAG, "WebView Submit Button exists. Clicking on it…");
        webViewSubmitButton.click();

        webViewSubmitButton.waitUntilGone(TIMEOUT);

        final var webViewGrantAccessButton = mDevice.findObject(new UiSelector()
                .instance(0)
                .className(Button.class));
        Log.d(TAG, "Waiting for WebView Grant Access Button…");
        webViewGrantAccessButton.waitForExists(TIMEOUT);
        Log.d(TAG, "WebView Grant Access Button exists. Clicking on it…");
        webViewGrantAccessButton.click();

        webView.waitUntilGone(TIMEOUT);

        mDevice.waitForIdle(TIMEOUT);

        Log.d(TAG, "Wait for Nextcloud files app…");
        Thread.sleep(3_000);
        Log.d(TAG, "Finishing setup…");
    }

    @Test
    public void test_01_importAccountIntoSampleApp() throws UiObjectNotFoundException, InterruptedException {
        Log.i(TAG, "Import account into sample app");
        launch(APP_SAMPLE);
        final var WAIT = 3_000;

        final var accountButton = mDevice.findObject(new UiSelector()
                .instance(0)
                .className(Button.class));
        accountButton.waitForExists(TIMEOUT);
        accountButton.click();

        mDevice.waitForWindowUpdate(null, TIMEOUT);

        final var radioAccount = mDevice.findObject(new UiSelector()
                .clickable(true)
                .instance(0));
        radioAccount.waitForExists(TIMEOUT);
        radioAccount.click();

        Thread.sleep(WAIT);

        final var okButton = mDevice.findObject(new UiSelector()
                .textContains("OK"));
        Log.d(TAG, "Waiting for OK Button…");
        okButton.waitForExists(TIMEOUT);
        Thread.sleep(WAIT);
        Log.d(TAG, "OK Button exists. Clicking on it…");
        okButton.click();
        Log.d(TAG, "OK Button clicked");

        Thread.sleep(WAIT);

        final var allowButton = mDevice.findObject(new UiSelector()
                .instance(1)
                .className(Button.class));
        Log.d(TAG, "Waiting for Allow Button…");
        allowButton.waitForExists(TIMEOUT);
        Log.d(TAG, "Allow Button exists. Clicking on it…");
        allowButton.click();
        Log.d(TAG, "Allow Button clicked");

        Log.d(TAG, "Waiting for finished import…");
        final var welcomeText = mDevice.findObject(new UiSelector().description("Filter"));
        welcomeText.waitForExists(TIMEOUT);
        Log.d(TAG, "Import finished.");

        Log.i(TAG, "Verify successful import…");
        final var expectedToContain = CONFIG_DISPLAY_NAME + " on Nextcloud";
        final var result = mDevice.findObject(new UiSelector().textContains(expectedToContain));
        result.waitForExists(TIMEOUT);
        Log.i(TAG, "Expected UI to display '" + expectedToContain + "'. Found: '" + result.getText() + "'.");
    }

    private void launch(@NonNull String packageName) {
        Log.d(TAG, "Launching " + packageName);
        mDevice.pressHome();
        final var context = getInstrumentation().getContext();
        context.startActivity(context
                .getPackageManager()
                .getLaunchIntentForPackage(packageName)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
        mDevice.wait(hasObject(By.pkg(packageName).depth(0)), TIMEOUT);
    }
}
