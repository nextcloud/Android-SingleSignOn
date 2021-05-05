package com.nextcloud.android.sso.api;

import java.io.Serializable;

public class QueryPair implements Serializable {
    public String first;
    public String second;

    public QueryPair(String first, String second) {
        this.first = first;
        this.second = second;
    }
}
