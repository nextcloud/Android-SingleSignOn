package com.nextcloud.android.sso.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ParsedResponse <T> {
    private T response;
    private Map<String, String> headers = new HashMap<>();

    public ParsedResponse(T response, ArrayList<AidlNetworkRequest.PlainHeader> headers) {
        this.response = response;
        for (AidlNetworkRequest.PlainHeader header : headers) {
            this.headers.put(header.getName(), header.getValue());
        }
    }

    public T getResponse() {
        return response;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
