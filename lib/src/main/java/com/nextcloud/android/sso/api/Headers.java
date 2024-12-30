package com.nextcloud.android.sso.api;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class Headers {
    private final Map<String, String> headers = new HashMap<>();

    public void put(String key, String value) {
        headers.put(key.toLowerCase(), value);
    }

    public String get(String key) {
        return headers.get(key.toLowerCase());
    }
}
