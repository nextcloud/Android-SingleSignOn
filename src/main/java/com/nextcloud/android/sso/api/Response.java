package com.nextcloud.android.sso.api;

import java.io.InputStream;
import java.util.ArrayList;

import androidx.annotation.Nullable;

public class Response {
    private InputStream body;
    private ArrayList<AidlNetworkRequest.PlainHeader> headers;

    public Response(InputStream inputStream, ArrayList<AidlNetworkRequest.PlainHeader> headers) {
        this.body = inputStream;
        this.headers = headers;
    }

    public ArrayList<AidlNetworkRequest.PlainHeader> getPlainHeaders() {
        return headers;
    }

    public InputStream getBody() {
        return body;
    }
    
    @Nullable
    public AidlNetworkRequest.PlainHeader getPlainHeader(String key) {
        for (AidlNetworkRequest.PlainHeader header: headers) {
            if (header.getName().equalsIgnoreCase(key)) {
                return header;
            }
        }
        
        return null;
    }
}
