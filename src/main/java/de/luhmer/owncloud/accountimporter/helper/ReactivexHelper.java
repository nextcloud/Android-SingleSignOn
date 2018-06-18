package de.luhmer.owncloud.accountimporter.helper;

import de.luhmer.owncloud.accountimporter.aidl.NextcloudRequest;
import de.luhmer.owncloud.accountimporter.api.NextcloudAPI;
import io.reactivex.Completable;
import io.reactivex.functions.Action;

public class ReactivexHelper {

    public static Completable WrapInCompletable(final NextcloudAPI nextcloudAPI, final NextcloudRequest request) {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                nextcloudAPI.performRequest(Void.class, request);
            }
        });
    }

}

