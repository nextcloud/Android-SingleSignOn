package de.luhmer.owncloud.accountimporter.helper;

import java.io.InputStream;

import de.luhmer.owncloud.accountimporter.aidl.NextcloudRequest;
import de.luhmer.owncloud.accountimporter.api.NextcloudAPI;
import okhttp3.ResponseBody;

public class Okhttp3Helper {

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
