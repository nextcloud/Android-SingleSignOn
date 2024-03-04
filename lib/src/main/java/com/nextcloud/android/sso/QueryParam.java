/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso;

import java.io.Serializable;

import androidx.annotation.Nullable;

public class QueryParam implements Serializable {

    private static final long serialVersionUID = 21523240203234211L; //assign a long value

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
