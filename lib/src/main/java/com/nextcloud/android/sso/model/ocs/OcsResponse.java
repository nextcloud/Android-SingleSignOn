/*
 * Nextcloud Android SingleSignOn Library
 *
 * SPDX-FileCopyrightText: 2021-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.nextcloud.android.sso.model.ocs;

import com.google.gson.annotations.SerializedName;

/**
 * <p>A generic wrapper for <a href="https://www.open-collaboration-services.org/">OpenCollaborationServices</a> responses.</p>
 * <p>This is a convenience class for API endpoints located at <code>/ocs/…</code> which all have an identical wrapping structure.<br>
 * It is usually <strong>not</strong> used in APIs of 3rd party server apps like <a href="https://deck.readthedocs.io/en/latest/API/">Deck</a> or <a href="https://github.com/nextcloud/notes/blob/master/docs/api/README.md">Notes</a></p>
 * <p>Example usage with Retrofit:</p>
 * <pre>
 * {@code
 * @GET("/ocs/v2.php/cloud/capabilities?format=json")
 * Call<OcsResponse < OcsCapabilitiesResponse>> getCapabilities();
 * }
 * </pre>
 *
 * @param <T> defines the payload type of this {@link OcsResponse}.
 */
@SuppressWarnings("unused, SpellCheckingInspection")
public class OcsResponse<T> {
    public final OcsWrapper<T> ocs;

    public OcsResponse(OcsWrapper<T> ocs) {
        this.ocs = ocs;
    }

    public class OcsWrapper<T> {
        public final OcsMeta meta;
        public final T data;

        public OcsWrapper(OcsMeta meta, T data) {
            this.meta = meta;
            this.data = data;
        }

        public class OcsMeta {
            public final String status;
            @SerializedName("statuscode")
            public final int statusCode;
            public final String message;

            public OcsMeta(String status, int statusCode, String message) {
                this.status = status;
                this.statusCode = statusCode;
                this.message = message;
            }
        }
    }
}