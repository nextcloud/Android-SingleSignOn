package com.nextcloud.android.sso.api;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class QueryParam implements Serializable {
    public String key;
    public String value;

    public QueryParam(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof QueryParam) {
            QueryParam other = (QueryParam) obj;
            return this.key.equals(other.key) && this.value.equals(other.value);
        } else {
            return false;
        }
    }
}
