package de.luhmer.owncloud.accountimporter.helper;

import java.io.InputStream;
import java.lang.reflect.Type;

import de.luhmer.owncloud.accountimporter.aidl.NextcloudRequest;
import de.luhmer.owncloud.accountimporter.api.NextcloudAPI;
import de.luhmer.owncloud.accountimporter.helper.BufferedSourceSSO;
import io.reactivex.Completable;
import io.reactivex.functions.Action;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SsoObervable {

    public static ResponseBody getResponseBodyFromRequest(NextcloudAPI nextcloudAPI, NextcloudRequest request) {
        try {
            InputStream os = nextcloudAPI.performNetworkRequest(request);
            return ResponseBody.create(null, 0, new BufferedSourceSSO(os));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseBody.create(null, "");
    }




}
